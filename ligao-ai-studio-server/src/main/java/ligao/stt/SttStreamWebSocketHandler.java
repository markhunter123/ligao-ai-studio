package ligao.stt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import ligao.llm.service.ModelProviderService;
import ligao.system.model.SystemSettingsDTO;
import ligao.system.service.SystemSettingService;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Streaming dictation WebSocket — the WeChat-style "type with your voice"
 * path behind the chat input box.
 *
 * <h2>Protocol (browser ⇄ this handler)</h2>
 * <ol>
 *   <li>Browser sends {@code {type:"init", language?, sampleRate?}}.</li>
 *   <li>Handler picks a mode and answers {@code {type:"ready", mode}}:
 *       <ul>
 *         <li>{@code stream} — DashScope is configured; audio is piped
 *             live into a Paraformer realtime session and interim
 *             transcripts come back as {@code {type:"partial", text}}
 *             (cumulative text so far) while the user speaks.</li>
 *         <li>{@code batch} — no streaming-capable provider; PCM is
 *             buffered and transcribed in one shot on stop. The UX degrades
 *             to "text appears when you finish" but still works.</li>
 *       </ul></li>
 *   <li>Browser streams raw 16-bit little-endian mono PCM as binary frames
 *       (~100 ms each, produced at wall-clock pace by the mic).</li>
 *   <li>Browser sends {@code {type:"stop"}} → handler replies with
 *       {@code {type:"final", text}}; or {@code {type:"cancel"}} →
 *       {@code {type:"cancelled"}}, nothing is finalized.</li>
 * </ol>
 *
 * <p>This is deliberately separate from {@code TalkModeWebSocketHandler}:
 * dictation only transcribes into the input box (no agent turn, no TTS), so
 * the fullscreen talk overlay was retired in favour of this inline flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SttStreamWebSocketHandler extends BinaryWebSocketHandler {

    /** DashScope WS endpoint for realtime inference. */
    private static final URI DASHSCOPE_WS_URL = URI.create("wss://dashscope.aliyuncs.com/api-ws/v1/inference/");

    /** Default model — same canonical realtime ASR the batch provider uses. */
    private static final String DEFAULT_MODEL = "paraformer-realtime-v2";

    /** Handshake + run-task ack budget for the upstream DashScope socket. */
    private static final long UPSTREAM_HANDSHAKE_MS = 8_000L;

    /** Budget for the finish-task round-trip after the user stops speaking. */
    private static final long FINISH_TIMEOUT_MS = 10_000L;

    /** Batch-mode PCM cap (~10 min of 16 kHz mono) so a forgotten "stop" can't OOM the heap. */
    private static final int MAX_BATCH_PCM_BYTES = 19_200_000;

    private final SystemSettingService systemSettingService;
    private final ModelProviderService modelProviderService;
    private final SttService sttService;
    private final ObjectMapper objectMapper;

    /** Shared HTTP client — see DashScopeSttProvider for the pool-reuse rationale. */
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final Map<String, DictationSession> sessions = new ConcurrentHashMap<>();

    /* ------------------------------------------------------------------ */
    /* Lifecycle                                                           */
    /* ------------------------------------------------------------------ */

    @Override
    public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) {
        log.debug("[SttStream] connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, CloseStatus status) {
        DictationSession st = sessions.remove(session.getId());
        if (st != null) {
            st.abortUpstream();
        }
        log.debug("[SttStream] disconnected: {} (status={})", session.getId(), status);
    }

    /* ------------------------------------------------------------------ */
    /* Text frames: init / stop / cancel                                   */
    /* ------------------------------------------------------------------ */

    @Override
    protected void handleTextMessage(org.springframework.web.socket.WebSocketSession ws, TextMessage message) {
        String type;
        Map<String, Object> data;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(message.getPayload(), Map.class);
            data = parsed;
            type = String.valueOf(data.get("type"));
        } catch (Exception e) {
            sendJson(ws, Map.of("type", "error", "message", "Malformed control message"));
            return;
        }

        switch (type) {
            case "init" -> handleInit(ws, data);
            case "stop" -> handleStop(ws);
            case "cancel" -> {
                DictationSession st = sessions.remove(ws.getId());
                if (st != null) st.abortUpstream();
                sendJson(ws, Map.of("type", "cancelled"));
            }
            default -> sendJson(ws, Map.of("type", "error", "message", "Unknown type: " + type));
        }
    }

    private void handleInit(org.springframework.web.socket.WebSocketSession ws, Map<String, Object> data) {
        SystemSettingsDTO config = systemSettingService.getAllSettings();
        if (!Boolean.TRUE.equals(config.getSttEnabled())) {
            sendJson(ws, Map.of("type", "error", "message", "STT 功能未启用，请在系统设置中开启"));
            return;
        }

        String language = data.get("language") instanceof String s && !s.isBlank() ? s : config.getLanguage();
        int sampleRate = data.get("sampleRate") instanceof Number n && n.intValue() > 0 ? n.intValue() : 16_000;

        DictationSession st = new DictationSession(language, sampleRate);
        sessions.put(ws.getId(), st);

        // Preferred path: live Paraformer stream. Any failure here silently
        // downgrades to batch mode rather than killing the feature — the UX
        // loses interim text but the final transcript still works.
        if (tryStartUpstreamStream(ws, st)) {
            sendJson(ws, Map.of("type", "ready", "mode", "stream"));
        } else {
            sendJson(ws, Map.of("type", "ready", "mode", "batch"));
        }
    }

    /** Opens the DashScope duplex session and issues run-task. Returns false on any failure. */
    private boolean tryStartUpstreamStream(org.springframework.web.socket.WebSocketSession ws, DictationSession st) {
        try {
            String apiKey = modelProviderService.getProviderConfig("dashscope").getApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                log.info("[SttStream] DashScope key missing — dictation downgrades to batch mode");
                return false;
            }
            String taskId = UUID.randomUUID().toString().replace("-", "");
            st.upstreamListener = new ParaformerStream(taskId, ws);
            WebSocket upstream = httpClient.newWebSocketBuilder()
                    .header("Authorization", "bearer " + apiKey)
                    .connectTimeout(Duration.ofMillis(UPSTREAM_HANDSHAKE_MS))
                    .buildAsync(DASHSCOPE_WS_URL, st.upstreamListener)
                    .get(UPSTREAM_HANDSHAKE_MS, TimeUnit.MILLISECONDS);
            st.upstream = upstream;

            upstream.sendText(buildRunTask(taskId, st.language, st.sampleRate), true)
                    .get(UPSTREAM_HANDSHAKE_MS, TimeUnit.MILLISECONDS);
            if (!st.upstreamListener.awaitTaskStarted(UPSTREAM_HANDSHAKE_MS, TimeUnit.MILLISECONDS)) {
                log.info("[SttStream] DashScope task-started timeout — downgrade to batch");
                st.abortUpstream();
                return false;
            }
            if (st.upstreamListener.failure() != null) {
                log.info("[SttStream] DashScope run-task rejected: {} — downgrade to batch", st.upstreamListener.failure());
                st.abortUpstream();
                return false;
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            st.abortUpstream();
            return false;
        } catch (Exception e) {
            log.warn("[SttStream] upstream stream setup failed ({}), downgrade to batch", e.getMessage());
            st.abortUpstream();
            return false;
        }
    }

    private void handleStop(org.springframework.web.socket.WebSocketSession ws) {
        DictationSession st = sessions.get(ws.getId());
        if (st == null) {
            sendJson(ws, Map.of("type", "error", "message", "Session not initialized"));
            return;
        }
        if (st.stopped) return;   // double Right-Alt press — idempotent
        st.stopped = true;

        if (st.upstream != null) {
            finishUpstream(ws, st);
        } else {
            finishBatch(ws, st);
        }
        sessions.remove(ws.getId());
        st.abortUpstream();
    }

    private void finishUpstream(org.springframework.web.socket.WebSocketSession ws, DictationSession st) {
        try {
            if (st.upstreamListener == null) {
                sendJson(ws, Map.of("type", "final", "text", ""));
                return;
            }
            st.upstream.sendText(buildFinishTask(st.upstreamListener.taskId), true)
                    .get(UPSTREAM_HANDSHAKE_MS, TimeUnit.MILLISECONDS);
            if (!st.upstreamListener.awaitTaskFinished(FINISH_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                log.warn("[SttStream] finish-task ack timeout — returning best-effort transcript");
            }
            String failure = st.upstreamListener.failure();
            String text = st.upstreamListener.cumulativeText();
            if ((failure != null) && text.isEmpty()) {
                sendJson(ws, Map.of("type", "error", "message", "识别失败: " + failure));
                return;
            }
            sendJson(ws, Map.of("type", "final", "text", text));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendJson(ws, Map.of("type", "error", "message", "识别被中断"));
        } catch (Exception e) {
            log.warn("[SttStream] finish failed: {}", e.getMessage());
            // Best effort: hand back whatever interim text arrived before the failure.
            String text = st.upstreamListener != null ? st.upstreamListener.cumulativeText() : "";
            if (text.isEmpty()) {
                sendJson(ws, Map.of("type", "error", "message", "识别结束失败: " + e.getMessage()));
            } else {
                sendJson(ws, Map.of("type", "final", "text", text));
            }
        }
    }

    private void finishBatch(org.springframework.web.socket.WebSocketSession ws, DictationSession st) {
        byte[] pcm = st.batchBuffer();
        if (pcm == null || pcm.length == 0) {
            sendJson(ws, Map.of("type", "final", "text", ""));
            return;
        }
        byte[] wav = wrapPcmAsWav(pcm, st.sampleRate);
        Map<String, Object> result = sttService.transcribe(wav, "dictation.wav", "audio/wav", st.language);
        if (Boolean.TRUE.equals(result.get("success"))) {
            Object text = result.get("text");
            sendJson(ws, Map.of("type", "final", "text", text != null ? text : ""));
        } else {
            Object err = result.get("error");
            sendJson(ws, Map.of("type", "error", "message", err != null ? err : "识别失败"));
        }
    }

    /* ------------------------------------------------------------------ */
    /* Binary frames: PCM audio                                            */
    /* ------------------------------------------------------------------ */

    @Override
    protected void handleBinaryMessage(org.springframework.web.socket.WebSocketSession ws, BinaryMessage message) {
        DictationSession st = sessions.get(ws.getId());
        if (st == null || st.stopped) return;
        java.nio.ByteBuffer payload = message.getPayload().duplicate().order(ByteOrder.LITTLE_ENDIAN);

        if (st.upstream != null) {
            try {
                st.upstream.request(1);
                // Chunks arrive at mic pace (~100 ms), so a bounded blocking send
                // applies natural back-pressure without pacing sleeps.
                st.upstream.sendBinary(payload, true).get(UPSTREAM_HANDSHAKE_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sendJson(ws, Map.of("type", "error", "message", "音频转发被中断"));
            } catch (Exception e) {
                log.warn("[SttStream] upstream send failed: {} — downgrading to batch", e.getMessage());
                st.abortUpstream();
                // Keep the session alive: buffered audio so far is lost (raw
                // frames were already handed to the socket), but the user can
                // finish the utterance in batch mode.
            }
        } else {
            st.appendBatch(payload);
        }
    }

    /* ------------------------------------------------------------------ */
    /* DashScope wire helpers (protocol mirrors DashScopeSttProvider)      */
    /* ------------------------------------------------------------------ */

    private String buildRunTask(String taskId, String language, int sampleRate) throws Exception {
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("format", "pcm");
        parameters.put("sample_rate", sampleRate);
        if (language != null && !language.isBlank()) {
            String hint = language.toLowerCase();
            int dash = hint.indexOf('-');
            if (dash > 0) hint = hint.substring(0, dash);
            parameters.put("language_hints", new String[]{hint});
        }
        Map<String, Object> message = Map.of(
                "header", Map.of("action", "run-task", "task_id", taskId, "streaming", "duplex"),
                "payload", Map.of(
                        "task_group", "audio",
                        "task", "asr",
                        "function", "recognition",
                        "model", DEFAULT_MODEL,
                        "parameters", parameters,
                        "input", Map.of()));
        return objectMapper.writeValueAsString(message);
    }

    private String buildFinishTask(String taskId) throws Exception {
        Map<String, Object> message = Map.of(
                "header", Map.of("action", "finish-task", "task_id", taskId, "streaming", "duplex"),
                "payload", Map.of("input", Map.of()));
        return objectMapper.writeValueAsString(message);
    }

    private void sendJson(org.springframework.web.socket.WebSocketSession ws, Map<String, Object> payload) {
        try {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            }
        } catch (Exception e) {
            log.debug("[SttStream] sendJson failed: {}", e.getMessage());
        }
    }

    /**
     * Wrap raw 16-bit mono PCM in a minimal 44-byte RIFF/WAVE header so the
     * batch fallback path can feed {@link SttService#transcribe} exactly the
     * WAV shape the browser-side encoder produces.
     */
    static byte[] wrapPcmAsWav(byte[] pcm, int sampleRate) {
        int dataSize = pcm.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream(44 + dataSize);
        writeAscii(out, "RIFF");
        writeInt32Le(out, 36 + dataSize);
        writeAscii(out, "WAVE");
        writeAscii(out, "fmt ");
        writeInt32Le(out, 16);
        writeInt16Le(out, 1);              // PCM
        writeInt16Le(out, 1);              // mono
        writeInt32Le(out, sampleRate);
        writeInt32Le(out, sampleRate * 2); // byte rate (16-bit mono)
        writeInt16Le(out, 2);              // block align
        writeInt16Le(out, 16);             // bits per sample
        writeAscii(out, "data");
        writeInt32Le(out, dataSize);
        out.writeBytes(pcm);
        return out.toByteArray();
    }

    private static void writeAscii(ByteArrayOutputStream out, String s) {
        for (int i = 0; i < s.length(); i++) out.write(s.charAt(i));
    }

    private static void writeInt32Le(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    private static void writeInt16Le(ByteArrayOutputStream out, int v) {
        out.write(v & 0xFF);
        out.write((v >> 8) & 0xFF);
    }

    /* ------------------------------------------------------------------ */
    /* Session state                                                       */
    /* ------------------------------------------------------------------ */

    /** Per-browser-connection state. Not thread-shared beyond its own WS callbacks. */
    private static final class DictationSession {
        final String language;
        final int sampleRate;
        WebSocket upstream;
        ParaformerStream upstreamListener;
        volatile boolean stopped = false;
        private final ByteArrayOutputStream batchPcm = new ByteArrayOutputStream();

        DictationSession(String language, int sampleRate) {
            this.language = language;
            this.sampleRate = sampleRate;
        }

        synchronized void appendBatch(java.nio.ByteBuffer chunk) {
            if (batchPcm.size() >= MAX_BATCH_PCM_BYTES) return;
            byte[] bytes = new byte[chunk.remaining()];
            chunk.get(bytes);
            batchPcm.writeBytes(bytes);
        }

        synchronized byte[] batchBuffer() {
            return batchPcm.toByteArray();
        }

        void abortUpstream() {
            WebSocket up = upstream;
            upstream = null;
            if (up != null) {
                try {
                    up.sendClose(WebSocket.NORMAL_CLOSURE, "done");
                } catch (Exception ignored) {
                    up.abort();
                }
            }
        }
    }

    /**
     * Upstream DashScope listener for the duplex bridge. Unlike the batch
     * provider's latch-only session, every {@code result-generated} event is
     * forwarded to the browser immediately as a cumulative partial, which is
     * what makes the "text appears while you speak" UX work.
     */
    final class ParaformerStream implements WebSocket.Listener {
        private final String taskId;
        private final org.springframework.web.socket.WebSocketSession browser;
        private final CountDownLatch taskStarted = new CountDownLatch(1);
        private final CountDownLatch taskFinished = new CountDownLatch(1);
        private final Map<Long, String> sentencesByBeginTime = new LinkedHashMap<>();
        private final StringBuilder textFrameBuf = new StringBuilder();
        private final AtomicReference<String> failureMessage = new AtomicReference<>();

        ParaformerStream(String taskId, org.springframework.web.socket.WebSocketSession browser) {
            this.taskId = taskId;
            this.browser = browser;
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            textFrameBuf.append(data);
            if (last) {
                handleMessage(textFrameBuf.toString());
                textFrameBuf.setLength(0);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            failureMessage.compareAndSet(null, "WS error: " + error.getMessage());
            taskStarted.countDown();
            taskFinished.countDown();
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (taskFinished.getCount() > 0) {
                failureMessage.compareAndSet(null,
                        "upstream closed before finish (status=" + statusCode + ")");
            }
            taskStarted.countDown();
            taskFinished.countDown();
            return null;
        }

        private void handleMessage(String json) {
            try {
                JsonNode node = objectMapper.readTree(json);
                String event = node.path("header").path("event").asText();
                switch (event) {
                    case "task-started" -> taskStarted.countDown();
                    case "result-generated" -> {
                        JsonNode sentence = node.path("payload").path("output").path("sentence");
                        if (sentence.isObject()) {
                            long beginTime = sentence.path("begin_time").asLong(0L);
                            String text = sentence.path("text").asText("");
                            sentencesByBeginTime.put(beginTime, text);
                            // Push the cumulative transcript so the input box
                            // rewrites its draft wholesale — interim events for
                            // the current sentence supersede earlier text.
                            sendJson(browser, Map.of("type", "partial", "text", cumulativeText()));
                        }
                    }
                    case "task-finished" -> taskFinished.countDown();
                    case "task-failed" -> {
                        String msg = node.path("header").path("error_message").asText("unknown");
                        String code = node.path("header").path("error_code").asText("");
                        failureMessage.compareAndSet(null, code.isEmpty() ? msg : code + " — " + msg);
                        taskStarted.countDown();
                        taskFinished.countDown();
                    }
                    default -> log.debug("[SttStream] upstream event '{}'", event);
                }
            } catch (Exception e) {
                log.warn("[SttStream] failed to parse upstream frame: {}", e.getMessage());
            }
        }

        boolean awaitTaskStarted(long timeout, TimeUnit unit) throws InterruptedException {
            return taskStarted.await(timeout, unit);
        }

        boolean awaitTaskFinished(long timeout, TimeUnit unit) throws InterruptedException {
            return taskFinished.await(timeout, unit);
        }

        String failure() {
            return failureMessage.get();
        }

        String cumulativeText() {
            StringBuilder sb = new StringBuilder();
            sentencesByBeginTime.values().forEach(sb::append);
            return sb.toString();
        }
    }
}

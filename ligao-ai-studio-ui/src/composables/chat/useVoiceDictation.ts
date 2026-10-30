/**
 * WeChat-style inline voice dictation for the chat input box.
 *
 * Flow: press the mic button (or the global Right Alt shortcut) → the mic
 * opens and raw PCM chunks stream over `/api/v1/stt/stream` to the backend,
 * which bridges them into a DashScope Paraformer realtime session. Cumulative
 * interim transcripts come back as `{type:"partial"}` frames while the user
 * is still speaking — the caller mirrors them into the textarea live. Right
 * Alt (or the stop button) finishes; the backend acks with `{type:"final"}`.
 *
 * If DashScope isn't configured the backend transparently downgrades to
 * batch mode (`ready.mode === 'batch'`): chunks are buffered and transcribed
 * in one shot on stop, so the feature works everywhere, just without the
 * while-speaking preview.
 */

import { ref, onBeforeUnmount, type Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { mcToast } from '@/composables/useMcToast'
import { WavRecorder } from '@/utils/wavEncoder'

export type DictationMode = 'stream' | 'batch'

export interface VoiceDictation {
    /** Mic is open and audio is being captured. */
    active: Ref<boolean>
    /** WS negotiated and the backend session is ready to take audio. */
    ready: Ref<boolean>
    /** Final transcription round-trip is in flight (mic already closed). */
    finalizing: Ref<boolean>
    /** 'stream' shows live partials; 'batch' only produces text on stop. */
    mode: Ref<DictationMode | null>
    /** Cumulative interim transcript (stream mode) — mirror into the input. */
    partial: Ref<string>
    /** Toggle recording — bound to the mic button and the Right Alt shortcut. */
    toggle: () => void
    /** Finish recording and wait for the final transcript. */
    stop: () => void
}

export function useVoiceDictation(options: {
    /** Gate — the toggle no-ops when false (no agent selected, loading, etc.). */
    enabled: () => boolean
}): VoiceDictation {
    const { t, locale } = useI18n()

    const active = ref(false)
    const ready = ref(false)
    const finalizing = ref(false)
    const mode = ref<DictationMode | null>(null)
    const partial = ref('')

    let ws: WebSocket | null = null
    let recorder: WavRecorder | null = null
    /** Chunks captured before the backend's `ready` ack — flushed on open. */
    let pendingChunks: ArrayBuffer[] = []
    /** Guards against a slow `final` after we've already torn down. */
    let forceCloseTimer: number | null = null

    function toggle() {
        if (active.value) {
            stop()
        } else {
            start()
        }
    }

    async function start() {
        if (active.value) return
        if (!options.enabled()) return

        partial.value = ''
        mode.value = null
        pendingChunks = []
        active.value = true

        // Mic first: the permission prompt (first run only) needs a user
        // gesture, and the keydown handler provides it. Audio chunks queue
        // until the backend session is ready.
        recorder = new WavRecorder()
        try {
            await recorder.start((pcm) => {
                if (ws && ws.readyState === WebSocket.OPEN && ready.value) {
                    ws.send(pcm)
                } else if (pendingChunks.length < 600) {
                    // ~100 s of audio headroom; the ready ack normally lands in <2 s.
                    pendingChunks.push(pcm)
                }
            })
        } catch (err) {
            console.warn('[Dictation] mic open failed', err)
            mcToast.error(t('voice.micError'))
            cleanup()
            return
        }

        const protocol = location.protocol === 'https:' ? 'wss:' : 'ws:'
        const token = localStorage.getItem('token')
        ws = new WebSocket(`${protocol}//${location.host}/api/v1/stt/stream${token ? '?token=' + token : ''}`)

        ws.onopen = () => {
            ws?.send(JSON.stringify({
                type: 'init',
                language: locale.value,
                sampleRate: 16000,
            }))
        }

        ws.onmessage = (event) => {
            if (event.data instanceof Blob) return
            try {
                const data = JSON.parse(event.data)
                switch (data.type) {
                    case 'ready':
                        mode.value = data.mode === 'stream' ? 'stream' : 'batch'
                        ready.value = true
                        flushPendingChunks()
                        break
                    case 'partial':
                        partial.value = String(data.text ?? '')
                        break
                    case 'final':
                        partial.value = String(data.text ?? '')
                        cleanup()
                        break
                    case 'cancelled':
                        cleanup()
                        break
                    case 'error':
                        mcToast.error(String(data.message || t('voice.failed')))
                        cleanup()
                        break
                }
            } catch {
                // ignore non-JSON frames
            }
        }

        ws.onerror = () => {
            if (!finalizing.value) {
                mcToast.error(t('voice.failed'))
            }
            cleanup()
        }

        ws.onclose = () => {
            // Normal teardown path: cleanup() closes the socket itself, so a
            // close event arriving afterwards is expected. An unexpected
            // close while still recording means the backend died — surface it.
            if (active.value && !finalizing.value) {
                mcToast.error(t('voice.failed'))
                cleanup()
            }
        }
    }

    function flushPendingChunks() {
        if (!ws || ws.readyState !== WebSocket.OPEN) return
        const queued = pendingChunks
        pendingChunks = []
        for (const chunk of queued) {
            ws.send(chunk)
        }
    }

    function stop() {
        if (!active.value || finalizing.value) return
        finalizing.value = true
        // Release the mic immediately — the user stopped talking; the final
        // transcript round-trip no longer needs it.
        const rec = recorder
        recorder = null
        rec?.stop().catch(() => {})
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({ type: 'stop' }))
            // Safety net: if `final` never arrives (backend restart, network
            // drop), tear down anyway so the button can't stick in red.
            forceCloseTimer = window.setTimeout(() => cleanup(), 15_000)
        } else {
            cleanup()
        }
    }

    function cleanup() {
        if (forceCloseTimer !== null) {
            clearTimeout(forceCloseTimer)
            forceCloseTimer = null
        }
        const socket = ws
        ws = null
        if (socket && socket.readyState <= WebSocket.OPEN) {
            try { socket.close() } catch { /* already closing */ }
        }
        if (recorder?.isActive()) {
            recorder.stop().catch(() => {})
        }
        recorder = null
        pendingChunks = []
        active.value = false
        ready.value = false
        finalizing.value = false
        mode.value = null
    }

    /* ------------------------------------------------------------------ */
    /* Right Alt global shortcut                                           */
    /* ------------------------------------------------------------------ */

    function onKeyDown(e: KeyboardEvent) {
        // AltRight only, no repeat, and not AltGr (some layouts emit
        // CtrlRight+AltRight for special characters — never hijack those).
        if (e.code !== 'AltRight' || e.repeat || e.ctrlKey) return
        e.preventDefault()
        toggle()
    }

    window.addEventListener('keydown', onKeyDown)

    onBeforeUnmount(() => {
        window.removeEventListener('keydown', onKeyDown)
        if (active.value && ws && ws.readyState === WebSocket.OPEN) {
            try { ws.send(JSON.stringify({ type: 'cancel' })) } catch { /* socket gone */ }
        }
        cleanup()
    })

    return { active, ready, finalizing, mode, partial, toggle, stop }
}

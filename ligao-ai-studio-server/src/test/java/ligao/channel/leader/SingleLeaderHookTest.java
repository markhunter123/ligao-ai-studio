package ligao.channel.leader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ligao.channel.ChannelMessageRouter;
import ligao.channel.feishu.FeishuChannelAdapter;
import ligao.channel.model.ChannelEntity;
import ligao.channel.qq.QQChannelAdapter;
import ligao.channel.wecom.WeComChannelAdapter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Behavioural test for the {@code requiresSingleLeader()} hook on the
 * Feishu and QQ adapters. This is what gates leader election in
 * {@code ChannelManager}, so a regression that flips the answer would
 * silently re-introduce the multi-instance connection-limit bug.
 */
class SingleLeaderHookTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChannelMessageRouter router = mock(ChannelMessageRouter.class);

    private ChannelEntity channel(String type, String configJson) {
        ChannelEntity e = new ChannelEntity();
        e.setId(1L);
        e.setName("test");
        e.setChannelType(type);
        e.setConfigJson(configJson);
        e.setEnabled(true);
        return e;
    }

    @Test
    @DisplayName("Feishu in WebSocket mode requires single leader (default mode)")
    void feishuWebsocketRequiresLeader() {
        FeishuChannelAdapter adapter = new FeishuChannelAdapter(
                channel("feishu", "{\"app_id\":\"x\",\"app_secret\":\"y\"}"),
                router, objectMapper);
        assertTrue(adapter.requiresSingleLeader(),
                "Default Feishu mode is websocket and must require single leader");
    }

    @Test
    @DisplayName("Feishu explicitly set to websocket mode requires single leader")
    void feishuExplicitWebsocketRequiresLeader() {
        FeishuChannelAdapter adapter = new FeishuChannelAdapter(
                channel("feishu",
                        "{\"app_id\":\"x\",\"app_secret\":\"y\",\"connection_mode\":\"websocket\"}"),
                router, objectMapper);
        assertTrue(adapter.requiresSingleLeader());
    }

    @Test
    @DisplayName("Feishu in webhook mode does NOT require single leader (load-balanced HTTP)")
    void feishuWebhookDoesNotRequireLeader() {
        FeishuChannelAdapter adapter = new FeishuChannelAdapter(
                channel("feishu",
                        "{\"app_id\":\"x\",\"app_secret\":\"y\",\"connection_mode\":\"webhook\"}"),
                router, objectMapper);
        assertFalse(adapter.requiresSingleLeader(),
                "Webhook callbacks are HTTP-fanned by the LB, so all nodes may subscribe");
    }

    @Test
    @DisplayName("QQ always requires single leader (gateway rejects duplicate IDENTIFY)")
    void qqAlwaysRequiresLeader() {
        QQChannelAdapter adapter = new QQChannelAdapter(
                channel("qq", "{\"app_id\":\"x\",\"client_secret\":\"y\"}"),
                router, objectMapper);
        assertTrue(adapter.requiresSingleLeader());
    }

    @Test
    @DisplayName("WeCom always requires single leader (WS-only aibot transport)")
    void wecomAlwaysRequiresLeader() {
        WeComChannelAdapter adapter = new WeComChannelAdapter(
                channel("wecom", "{\"bot_id\":\"x\",\"secret\":\"y\"}"),
                router, objectMapper,
                mock(ligao.channel.notification.ApprovalNotificationService.class),
                mock(ligao.channel.wecom.cards.WeComCardDispatcher.class),
                mock(ligao.channel.wecom.WeComKeepaliveScheduler.class));
        assertTrue(adapter.requiresSingleLeader());
    }
}

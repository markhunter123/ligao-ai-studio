package ligao.cron;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ligao.agent.context.ChannelTarget;
import ligao.agent.context.ChatOrigin;
import ligao.agent.model.AgentEntity;
import ligao.agent.repository.AgentMapper;
import ligao.cron.model.CronJobEntity;
import ligao.cron.model.DeliveryConfig;

/**
 * RFC-063r §2.2: factory that builds a {@link ChatOrigin} for a cron-triggered
 * agent invocation. Lives in {@code ligao.cron} so the dependency arrow
 * points {@code cron → agent} only — symmetric with
 * {@code ChannelChatOriginFactory} in {@code ligao.channel}.
 *
 * <p>workspaceId is reverse-resolved from {@code agent.workspaceId} (with the
 * legacy {@code 1L} fallback to keep behavior identical to
 * {@code CronJobService.executeJob}). workspaceBasePath is intentionally not
 * persisted — the value is derived at run time from the agent's workspace
 * configuration, matching the previous {@code ToolExecutionContext.workspaceBasePath()}
 * semantics.
 */
@Component
@RequiredArgsConstructor
public class CronChatOriginFactory {

    private final AgentMapper agentMapper;

    public ChatOrigin from(CronJobEntity job, String conversationId) {
        AgentEntity agent = job.getAgentId() != null ? agentMapper.selectById(job.getAgentId()) : null;
        Long workspaceId = agent != null && agent.getWorkspaceId() != null ? agent.getWorkspaceId() : 1L;

        DeliveryConfig dc = job.getDeliveryConfig();
        ChannelTarget target = dc != null ? dc.toChannelTarget() : null;

        return ChatOrigin.cron(conversationId, workspaceId, /* workspaceBasePath */ null,
                job.getChannelId(), target);
    }
}

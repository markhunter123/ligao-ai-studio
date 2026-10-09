package ligao.channel.verifier;

import java.util.Map;

/**
 * Draft channel config submitted to the wizard's Verify step. Carries only
 * what a verifier needs — no entity ID, no audit context — because preflight
 * runs before the row exists in {@code ligao_channel}.
 *
 * @author Zhouyin.Wei
 */
public record VerificationRequest(
        String channelType,
        Map<String, Object> config,
        Long workspaceId
) {
}

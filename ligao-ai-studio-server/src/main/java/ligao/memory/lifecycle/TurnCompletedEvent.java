package ligao.memory.lifecycle;

/**
 * Published after syncAll completes for a turn.
 *
 * @param context        the turn context
 * @param assistantReply the LLM response text
 * @author Zhouyin.Wei
 */
public record TurnCompletedEvent(TurnContext context, String assistantReply) {}

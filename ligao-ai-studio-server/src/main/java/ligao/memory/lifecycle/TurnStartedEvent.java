package ligao.memory.lifecycle;

/**
 * Published after prefetchAll completes, before the LLM call.
 *
 * @param context the turn context
 * @author Zhouyin.Wei
 */
public record TurnStartedEvent(TurnContext context) {}

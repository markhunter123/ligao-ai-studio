package ligao.memory.search;

import java.time.LocalDateTime;

/**
 * Session search result — a matched message from conversation history.
 *
 * @author Zhouyin.Wei
 */
public record SessionSearchResult(
        String conversationId,
        String title,
        String snippet,
        String role,
        LocalDateTime time,
        double relevance
) {
}

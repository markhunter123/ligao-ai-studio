package ligao.memory.service;

/**
 * A candidate that was scored but not adopted into MEMORY.md during a dream.
 *
 * @author Zhouyin.Wei
 */
public record RejectedEntry(
        Long recallId,
        String filename,
        String snippetPreview,
        double score,
        int reviewCount
) {}

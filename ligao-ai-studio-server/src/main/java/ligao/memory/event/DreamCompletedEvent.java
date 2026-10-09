package ligao.memory.event;

import ligao.memory.service.DreamReport;

/**
 * Published when a dream consolidation completes successfully.
 *
 * @param report the structured dream report
 * @author Zhouyin.Wei
 */
public record DreamCompletedEvent(DreamReport report) {}

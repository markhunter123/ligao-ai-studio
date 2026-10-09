package ligao.memory.event;

import ligao.memory.service.DreamReport;

/**
 * Published when a dream consolidation fails.
 *
 * @param report the structured dream report (status=FAILED)
 * @author Zhouyin.Wei
 */
public record DreamFailedEvent(DreamReport report) {}

package ligao.wiki.job.fallback;

import ligao.wiki.job.WikiJobStep;
import ligao.wiki.job.model.WikiProcessingJobEntity;

import java.util.Optional;

/**
 * RFC-030: Chain of responsibility for model fallback selection.
 */
public interface ModelFallbackHandler {

    Optional<Long> handle(WikiProcessingJobEntity job, WikiJobStep step, String errorCode);

    void setNext(ModelFallbackHandler next);
}

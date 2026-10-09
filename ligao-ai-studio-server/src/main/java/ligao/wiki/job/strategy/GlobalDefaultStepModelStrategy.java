package ligao.wiki.job.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ligao.llm.service.ModelConfigService;
import ligao.wiki.job.WikiJobStep;
import ligao.wiki.job.model.WikiProcessingJobEntity;
import ligao.wiki.model.WikiKnowledgeBaseEntity;

/**
 * RFC-030: Final fallback strategy — uses the system default model.
 * Cheap steps (ROUTE, ENRICH, SUMMARY) prefer a lighter/cheaper model;
 * strong steps (CREATE_PAGE, MERGE_PAGE) use the default model.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class GlobalDefaultStepModelStrategy implements WikiStepModelStrategy {

    private final ModelConfigService modelConfigService;

    @Override
    public boolean supports(WikiJobStep step) { return true; }

    @Override
    public Long selectModelId(WikiProcessingJobEntity job, WikiKnowledgeBaseEntity kb, WikiJobStep step) {
        // RFC-030: cheap steps (ROUTE, ENRICH, SUMMARY) should ideally use a lighter model,
        // but ModelConfigService has no "cheapest chat model" concept yet.
        // When per-step pricing metadata is added, this switch can differentiate.
        return modelConfigService.getDefaultModel().getId();
    }
}

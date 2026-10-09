package ligao.wiki.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ligao.common.result.R;
import ligao.wiki.model.WikiTransformationEntity;
import ligao.wiki.model.WikiTransformationRunEntity;
import ligao.wiki.service.WikiKnowledgeBaseService;
import ligao.wiki.service.WikiTransformationAggregator;
import ligao.wiki.service.WikiTransformationExecutor;
import ligao.wiki.service.WikiTransformationService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WikiTransformationControllerTest {

    private WikiTransformationService transformationService;
    private WikiTransformationController controller;

    @BeforeEach
    void setUp() {
        transformationService = mock(WikiTransformationService.class);
        controller = new WikiTransformationController(
                transformationService,
                mock(WikiTransformationExecutor.class),
                mock(WikiTransformationAggregator.class),
                mock(WikiKnowledgeBaseService.class));
    }

    @Test
    void applyMissingTemplateReturns404Envelope() {
        when(transformationService.getById(99L)).thenReturn(null);

        R<WikiTransformationRunEntity> response = controller.apply(
                99L, Map.of("rawId", 1L), false, 1L);

        assertEquals(404, response.getCode());
    }

    @Test
    void applyWithRawIdAndPageIdReturns400Envelope() {
        WikiTransformationEntity transformation = new WikiTransformationEntity();
        transformation.setId(99L);
        transformation.setWorkspaceId(1L);
        when(transformationService.getById(99L)).thenReturn(transformation);

        R<WikiTransformationRunEntity> response = controller.apply(
                99L, Map.of("rawId", 1L, "pageId", 2L), false, 1L);

        assertEquals(400, response.getCode());
    }
}

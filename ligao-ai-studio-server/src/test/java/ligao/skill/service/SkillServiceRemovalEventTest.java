package ligao.skill.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import ligao.skill.event.SkillRemovedEvent;
import ligao.skill.lifecycle.SkillLifecycleService;
import ligao.skill.model.SkillEntity;
import ligao.skill.repository.SkillFileMapper;
import ligao.skill.repository.SkillMapper;
import ligao.skill.runtime.SkillRuntimeService;
import ligao.skill.secret.SkillSecretService;
import ligao.skill.workspace.SkillWorkspaceManager;
import ligao.skill.workspace.SkillWorkspaceProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Issue #127 — verifies the delete paths publish {@link SkillRemovedEvent}
 * so the agent-binding listener can scrub {@code ligao_agent_skill} orphans.
 *
 * <p>Earlier behavior dropped only {@code ligao_skill}/{@code ligao_skill_file}
 * /secrets/workspace and left binding rows pointing at a vanished skill id,
 * which is what users saw as "agent still shows N skills bound".
 */
class SkillServiceRemovalEventTest {

    @Test
    @DisplayName("uninstallSkill publishes SkillRemovedEvent with the row's id and name")
    void uninstallPublishesEvent() {
        SkillMapper mapper = mock(SkillMapper.class);
        SkillFileMapper fileMapper = mock(SkillFileMapper.class);
        SkillWorkspaceManager workspaceManager = mock(SkillWorkspaceManager.class);
        SkillWorkspaceProperties workspaceProps = mock(SkillWorkspaceProperties.class);
        SkillSecretService secretService = mock(SkillSecretService.class);
        SkillRuntimeService runtimeService = mock(SkillRuntimeService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        SkillEntity row = new SkillEntity();
        row.setId(42L);
        row.setName("pdf");
        row.setBuiltin(false);
        when(mapper.selectById(42L)).thenReturn(row);
        // Workspace policy other than "archive" keeps the test focused on the
        // event publish behavior — the archive branch has its own coverage.
        when(workspaceProps.getDeletePolicy()).thenReturn("purge");

        SkillService service = new SkillService(
                mapper, fileMapper, workspaceManager, workspaceProps, secretService, publisher,
                mock(SkillLifecycleService.class));
        service.setRuntimeService(runtimeService);

        service.uninstallSkill(42L);

        ArgumentCaptor<SkillRemovedEvent> captor = ArgumentCaptor.forClass(SkillRemovedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        SkillRemovedEvent event = captor.getValue();
        assertEquals(42L, event.skillId());
        assertEquals("pdf", event.skillName());
    }

    @Test
    @DisplayName("hardDeleteSkill publishes SkillRemovedEvent for the admin-only delete path")
    void hardDeletePublishesEvent() {
        SkillMapper mapper = mock(SkillMapper.class);
        SkillFileMapper fileMapper = mock(SkillFileMapper.class);
        SkillWorkspaceManager workspaceManager = mock(SkillWorkspaceManager.class);
        SkillWorkspaceProperties workspaceProps = mock(SkillWorkspaceProperties.class);
        SkillSecretService secretService = mock(SkillSecretService.class);
        SkillRuntimeService runtimeService = mock(SkillRuntimeService.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        SkillEntity row = new SkillEntity();
        row.setId(99L);
        row.setName("legacy-cleanup");
        row.setBuiltin(false);
        when(mapper.selectById(99L)).thenReturn(row);
        when(fileMapper.deleteBySkillId(99L)).thenReturn(0);

        SkillService service = new SkillService(
                mapper, fileMapper, workspaceManager, workspaceProps, secretService, publisher,
                mock(SkillLifecycleService.class));
        service.setRuntimeService(runtimeService);

        service.hardDeleteSkill(99L);

        ArgumentCaptor<SkillRemovedEvent> captor = ArgumentCaptor.forClass(SkillRemovedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        SkillRemovedEvent event = captor.getValue();
        assertEquals(99L, event.skillId());
        assertEquals("legacy-cleanup", event.skillName());
    }
}

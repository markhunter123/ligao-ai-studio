package ligao.agent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ligao.agent.model.AgentEntity;
import ligao.agent.model.TemplateDTO;
import ligao.agent.service.TemplateService;
import ligao.auth.model.UserEntity;
import ligao.auth.service.AuthService;
import ligao.common.result.R;
import ligao.exception.LigaoException;
import ligao.workspace.core.annotation.RequireWorkspaceRole;

import java.util.List;

/**
 * Agent 模板接口
 *
 * @author Zhouyin.Wei
 */
@Tag(name = "Agent Templates")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final AuthService authService;

    @Operation(summary = "获取模板列表")
    @GetMapping
    public R<List<TemplateDTO>> list() {
        return R.ok(templateService.listTemplates());
    }

    @Operation(summary = "应用模板创建Agent")
    @PostMapping("/{id}/apply")
    @RequireWorkspaceRole("member")
    public R<AgentEntity> apply(
            @PathVariable String id,
            @RequestHeader(value = "X-Workspace-Id", required = false) Long workspaceId,
            // Accept-Language is forwarded by the frontend (zh-CN, zh, en, en-US, ...)
            // so the new agent's display name matches the user's locale —
            // a Chinese user hiring "客服助理" should not get an English
            // "Customer Support" agent in their list.
            @RequestHeader(value = "Accept-Language", required = false) String acceptLanguage,
            Authentication auth) {
        long wsId = workspaceId != null ? workspaceId : 1L;
        Long userId = resolveUserId(auth);
        return R.ok(templateService.applyTemplate(id, wsId, userId, acceptLanguage));
    }

    private Long resolveUserId(Authentication auth) {
        if (auth == null) {
            throw new LigaoException("err.auth.unauthenticated", 401, "Not authenticated");
        }
        UserEntity user = authService.findByUsername(auth.getName());
        if (user == null) {
            throw new LigaoException("err.auth.user_not_found", 401, "User not found: " + auth.getName());
        }
        return user.getId();
    }
}

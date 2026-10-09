-- V20: Purge soft-deleted rows from all tables and retire soft-delete semantics.
-- @TableLogic has been removed from all entities — the project no longer
-- supports soft-delete. Clear residual deleted=1 rows so queries that still
-- reference the `deleted` column (or raw SQL in service layer) continue to
-- behave consistently. The `deleted` column itself is retained with its
-- NOT NULL DEFAULT 0 constraint for schema compatibility.
DELETE FROM ligao_agent               WHERE deleted = 1;
DELETE FROM ligao_agent_skill         WHERE deleted = 1;
DELETE FROM ligao_agent_tool          WHERE deleted = 1;
DELETE FROM ligao_channel             WHERE deleted = 1;
DELETE FROM ligao_channel_session     WHERE deleted = 1;
DELETE FROM ligao_conversation        WHERE deleted = 1;
DELETE FROM ligao_cron_job            WHERE deleted = 1;
DELETE FROM ligao_datasource          WHERE deleted = 1;
DELETE FROM ligao_mcp_server          WHERE deleted = 1;
DELETE FROM ligao_memory_recall       WHERE deleted = 1;
DELETE FROM ligao_message             WHERE deleted = 1;
DELETE FROM ligao_model_config        WHERE deleted = 1;
DELETE FROM ligao_plan                WHERE deleted = 1;
DELETE FROM ligao_plugin              WHERE deleted = 1;
DELETE FROM ligao_skill               WHERE deleted = 1;
DELETE FROM ligao_sub_plan            WHERE deleted = 1;
DELETE FROM ligao_tool                WHERE deleted = 1;
DELETE FROM ligao_tool_approval       WHERE deleted = 1;
DELETE FROM ligao_tool_guard_audit_log WHERE deleted = 1;
DELETE FROM ligao_tool_guard_rule     WHERE deleted = 1;
DELETE FROM ligao_user                WHERE deleted = 1;
DELETE FROM ligao_wiki_chunk          WHERE deleted = 1;
DELETE FROM ligao_wiki_knowledge_base WHERE deleted = 1;
DELETE FROM ligao_wiki_page           WHERE deleted = 1;
DELETE FROM ligao_wiki_raw_material   WHERE deleted = 1;
DELETE FROM ligao_workspace           WHERE deleted = 1;
DELETE FROM ligao_workspace_file      WHERE deleted = 1;
DELETE FROM ligao_workspace_member    WHERE deleted = 1;

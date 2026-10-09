-- RFC-083: workspace-isolate cron jobs
-- (issue: https://github.com/ligao-studio/ligao-ai-studio/issues/37).

ALTER TABLE ligao_cron_job ADD COLUMN IF NOT EXISTS workspace_id BIGINT NOT NULL DEFAULT 1;

CREATE INDEX IF NOT EXISTS idx_cron_job_workspace ON ligao_cron_job(workspace_id, deleted);

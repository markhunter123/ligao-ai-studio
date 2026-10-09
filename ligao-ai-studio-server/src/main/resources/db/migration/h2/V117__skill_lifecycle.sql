-- Skill lifecycle columns: drive the time-window archival state machine
-- (active -> stale -> archived) for agent-created skills.
--
--   lifecycle_state  — current state; defaults to 'active'.
--   pinned           — user-pinned skills are never auto-archived.
--   archived_at      — wall-clock time the skill entered the archived state.
--   last_activity_at — cached activity anchor, mirrored from
--                      ligao_skill_usage_stat.last_loaded_at so the daily
--                      sweep is a single indexed select instead of a join.
--
-- last_activity_at uses the same TIMESTAMP type as
-- ligao_skill_usage_stat.last_loaded_at so comparisons keep precision.

ALTER TABLE ligao_skill ADD COLUMN IF NOT EXISTS lifecycle_state VARCHAR(16) DEFAULT 'active';
ALTER TABLE ligao_skill ADD COLUMN IF NOT EXISTS pinned TINYINT(1) DEFAULT 0;
ALTER TABLE ligao_skill ADD COLUMN IF NOT EXISTS archived_at TIMESTAMP NULL;
ALTER TABLE ligao_skill ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP NULL;

CREATE INDEX IF NOT EXISTS idx_skill_lifecycle_state ON ligao_skill(lifecycle_state);
CREATE INDEX IF NOT EXISTS idx_skill_last_activity_at ON ligao_skill(last_activity_at);

-- One-time backfill: existing rows take their newest usage tick as the
-- activity anchor. Rows with no usage stat stay NULL and fall through to
-- create_time at query time via the anchor() helper.
UPDATE ligao_skill SET last_activity_at = (
  SELECT MAX(last_loaded_at) FROM ligao_skill_usage_stat s
   WHERE s.skill_name = ligao_skill.name
)
WHERE last_activity_at IS NULL;

-- Defensive: ensure no NULL state survives even if a prior partial run left
-- the column unset (the ADD COLUMN DEFAULT already covers the normal path).
UPDATE ligao_skill SET lifecycle_state = 'active' WHERE lifecycle_state IS NULL;

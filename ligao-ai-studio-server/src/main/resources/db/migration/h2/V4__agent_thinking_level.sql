-- V4: Add default_thinking_level to ligao_agent
-- Supports: off / low / medium / high / max (null = follow model default)
ALTER TABLE ligao_agent ADD COLUMN IF NOT EXISTS default_thinking_level VARCHAR(32) DEFAULT NULL;

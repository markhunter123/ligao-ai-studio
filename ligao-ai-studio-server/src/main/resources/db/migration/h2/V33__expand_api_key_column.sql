-- V33: Expand ligao_model_provider.api_key from VARCHAR(256) to VARCHAR(512)
-- Bailian Token Plan keys exceed 256 chars (observed: 298 chars).
ALTER TABLE ligao_model_provider ALTER COLUMN api_key VARCHAR(512) NOT NULL DEFAULT '';

-- V91: Mirror MySQL widening of ligao_message.content / content_parts and
-- ligao_skill.skill_content. H2's TEXT is already CLOB (effectively unbounded)
-- so the change is a no-op semantically; it keeps both dialects in sync.

ALTER TABLE ligao_message ALTER COLUMN content CLOB;
ALTER TABLE ligao_message ALTER COLUMN content_parts CLOB;
ALTER TABLE ligao_skill ALTER COLUMN skill_content CLOB;

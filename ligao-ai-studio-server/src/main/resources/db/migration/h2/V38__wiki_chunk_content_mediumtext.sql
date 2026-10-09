-- V38: Expand ligao_wiki_chunk.content to CLOB (H2 equivalent of MEDIUMTEXT)
-- H2's TEXT is already effectively unbounded, but align with MySQL change for consistency.
ALTER TABLE ligao_wiki_chunk ALTER COLUMN content CLOB NOT NULL;

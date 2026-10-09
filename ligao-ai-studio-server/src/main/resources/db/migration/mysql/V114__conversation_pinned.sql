-- Per-conversation pin flag. See the h2 sibling for the rationale.
-- MySQL has no ADD COLUMN IF NOT EXISTS; guard via INFORMATION_SCHEMA.

SET @c := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ligao_conversation'
             AND COLUMN_NAME = 'pinned');
SET @s := IF(@c = 0, 'ALTER TABLE ligao_conversation ADD COLUMN pinned INT DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

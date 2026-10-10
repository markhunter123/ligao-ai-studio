-- Product decision (2026-10): Slack / Telegram / Discord channel support is
-- removed from the platform. Purge their channel rows and cached sessions,
-- and soft-delete conversations that originated from those channels so they
-- no longer surface in the UI. Messages are kept for audit but hidden with
-- the conversation.

DELETE FROM ligao_channel
WHERE channel_type IN ('slack', 'telegram', 'discord');

DELETE FROM ligao_channel_session
WHERE channel_type IN ('slack', 'telegram', 'discord')
   OR conversation_id LIKE 'slack:%'
   OR conversation_id LIKE 'telegram:%'
   OR conversation_id LIKE 'discord:%';

UPDATE ligao_conversation
SET deleted = 1
WHERE deleted = 0
  AND (conversation_id LIKE 'slack:%'
    OR conversation_id LIKE 'telegram:%'
    OR conversation_id LIKE 'discord:%');

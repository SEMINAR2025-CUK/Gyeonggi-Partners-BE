-- Add version column for optimistic locking
ALTER TABLE discussion_rooms
ADD COLUMN version BIGINT DEFAULT 0;

-- Set existing rows to version 0
UPDATE discussion_rooms
SET version = 0
WHERE version IS NULL;

-- Make version NOT NULL after initial data migration
ALTER TABLE discussion_rooms
ALTER COLUMN version SET NOT NULL;

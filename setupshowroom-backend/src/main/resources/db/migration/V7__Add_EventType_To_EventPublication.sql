-- Add event_type column to event_publication table
ALTER TABLE event_publication ADD COLUMN event_type VARCHAR(255);

-- Add event_type column to event_publication_archive table
ALTER TABLE event_publication_archive ADD COLUMN event_type VARCHAR(255); 
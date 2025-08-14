-- Drop and recreate event_publication table
DROP TABLE IF EXISTS event_publication;
CREATE TABLE event_publication (
    id UUID NOT NULL,
    completion_date TIMESTAMP WITHOUT TIME ZONE,
    event_type VARCHAR(255),
    listener_id VARCHAR(255),
    publication_date TIMESTAMP WITHOUT TIME ZONE,
    serialized_event TEXT,
    CONSTRAINT pk_event_publication PRIMARY KEY (id)
);

-- Drop and recreate event_publication_archive table
DROP TABLE IF EXISTS event_publication_archive;
CREATE TABLE event_publication_archive (
    id UUID NOT NULL,
    completion_date TIMESTAMP WITHOUT TIME ZONE,
    event_type VARCHAR(255),
    listener_id VARCHAR(255),
    publication_date TIMESTAMP WITHOUT TIME ZONE,
    serialized_event TEXT,
    CONSTRAINT pk_event_publication_archive PRIMARY KEY (id)
); 
CREATE TABLE report
(
    id               VARCHAR(26)                 NOT NULL,
    type             VARCHAR(255),
    description      VARCHAR(500)                NOT NULL,
    reported_item_id VARCHAR(26)                 NOT NULL,
    user_id          VARCHAR(26),
    deleted          BOOLEAN                     NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    deleted_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_report PRIMARY KEY (id)
);

ALTER TABLE report
    ADD CONSTRAINT FK_REPORT_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

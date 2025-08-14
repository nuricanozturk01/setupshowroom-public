CREATE TABLE notification
(
  id          VARCHAR(26)  NOT NULL,
  title       VARCHAR(255) NOT NULL,
  read        BOOLEAN      NOT NULL,
  description VARCHAR(255) NOT NULL,
  type        VARCHAR(255) NOT NULL,
  action      VARCHAR(255) NOT NULL,
  deleted     BOOLEAN      NOT NULL,
  user_id     VARCHAR(26),
  created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at  TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_notification PRIMARY KEY (id)
);

ALTER TABLE notification
  ADD CONSTRAINT FK_NOTIFICATION_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE favorite_product DROP COLUMN price;

ALTER TABLE favorite_product
  ADD COLUMN url VARCHAR(1000);


ALTER TABLE "like"
  ADD CONSTRAINT user_like_fk_constraint UNIQUE (user_id, setup_id);

ALTER TABLE favorite
  ADD CONSTRAINT user_favorite_fk_constraint UNIQUE (user_id, setup_id);

ALTER TABLE "notification"
  ADD COLUMN "to" VARCHAR(26);

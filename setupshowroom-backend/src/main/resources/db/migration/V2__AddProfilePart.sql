CREATE TABLE favorite_product
(
  id                        VARCHAR(26)  NOT NULL,
  name                      VARCHAR(150) NOT NULL,
  price                     VARCHAR(150) NOT NULL,
  deleted                   BOOLEAN      NOT NULL,
  created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at                TIMESTAMP WITHOUT TIME ZONE,
  favorite_product_group_id VARCHAR(26),
  CONSTRAINT pk_favorite_product PRIMARY KEY (id)
);

CREATE TABLE favorite_product_group
(
  id         VARCHAR(26)  NOT NULL,
  name       VARCHAR(255) NOT NULL,
  deleted    BOOLEAN      NOT NULL,
  user_id    VARCHAR(26),
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_favorite_product_group PRIMARY KEY (id)
);

CREATE TABLE system_requirement
(
  id           VARCHAR(26) NOT NULL,
  cpu          VARCHAR(150),
  gpu          VARCHAR(150),
  ram          VARCHAR(150),
  storage      VARCHAR(150),
  motherboards VARCHAR(150),
  psu          VARCHAR(150),
  "case"       VARCHAR(150),
  monitor      VARCHAR(150),
  keyboard     VARCHAR(150),
  mouse        VARCHAR(150),
  headset      VARCHAR(150),
  other        VARCHAR(1000),
  deleted      BOOLEAN     NOT NULL,
  user_id      VARCHAR(26),
  created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at   TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_system_requirement PRIMARY KEY (id)
);

ALTER TABLE setup_categories
  ADD categories VARCHAR(255);

ALTER TABLE setup_images
  ADD images VARCHAR(255);

ALTER TABLE setup_videos
  ADD videos VARCHAR(255);

ALTER TABLE system_requirement
  ADD CONSTRAINT uc_system_requirement_user UNIQUE (user_id);

ALTER TABLE favorite_product_group
  ADD CONSTRAINT FK_FAVORITE_PRODUCT_GROUP_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE favorite_product
  ADD CONSTRAINT FK_FAVORITE_PRODUCT_ON_FAVORITEPRODUCTGROUP FOREIGN KEY (favorite_product_group_id) REFERENCES favorite_product_group (id);

ALTER TABLE system_requirement
  ADD CONSTRAINT FK_SYSTEM_REQUIREMENT_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE setup_categories
DROP
COLUMN category;

ALTER TABLE setup_images
DROP
COLUMN image;

ALTER TABLE setup_videos
DROP
COLUMN video;

ALTER TABLE favorite
  ALTER COLUMN deleted SET NOT NULL;

ALTER TABLE "like"
  ALTER COLUMN deleted SET NOT NULL;

ALTER TABLE setup
  ALTER COLUMN deleted SET NOT NULL;

ALTER TABLE "user"
  ALTER COLUMN deleted SET NOT NULL;

ALTER TABLE user_preference
  ALTER COLUMN deleted SET NOT NULL;

ALTER TABLE "user"
  ALTER COLUMN enabled SET NOT NULL;

ALTER TABLE "user"
  ALTER COLUMN locked SET NOT NULL;

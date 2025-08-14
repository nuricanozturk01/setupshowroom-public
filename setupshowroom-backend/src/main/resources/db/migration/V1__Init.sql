CREATE TABLE event_publication
(
  id              UUID NOT NULL,
  completion_date TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_event_publication PRIMARY KEY (id)
);

CREATE TABLE comment
(
  id         VARCHAR(26)  NOT NULL,
  setup_id   VARCHAR(26)  NOT NULL,
  content    VARCHAR(500) NOT NULL,
  parent_id  VARCHAR(26),
  depth      INTEGER      NOT NULL,
  user_id    VARCHAR(26)  NOT NULL,
  deleted    BOOLEAN      NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_comment PRIMARY KEY (id)
);

CREATE TABLE event_publication_archive
(
  id              UUID NOT NULL,
  completion_date TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_event_publication_archive PRIMARY KEY (id)
);

CREATE TABLE favorite
(
  id         VARCHAR(26) NOT NULL,
  user_id    VARCHAR(26),
  setup_id   VARCHAR(26),
  deleted    BOOLEAN,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_favorite PRIMARY KEY (id)
);

CREATE TABLE "like"
(
  id         VARCHAR(26) NOT NULL,
  user_id    VARCHAR(26),
  setup_id   VARCHAR(26),
  deleted    BOOLEAN,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_like PRIMARY KEY (id)
);

CREATE TABLE setup
(
  id          VARCHAR(26)  NOT NULL,
  title       VARCHAR(50)  NOT NULL,
  description VARCHAR(500) NOT NULL,
  user_id     VARCHAR(26),
  deleted     BOOLEAN,
  created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at  TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_setup PRIMARY KEY (id)
);

CREATE TABLE setup_images
(
  setup_id VARCHAR(26) NOT NULL,
  image    VARCHAR(255)
);

CREATE TABLE setup_tag
(
  setup_id VARCHAR(26) NOT NULL,
  tag_id   VARCHAR(26) NOT NULL
);

CREATE TABLE setup_videos
(
  setup_id VARCHAR(26) NOT NULL,
  video    VARCHAR(255)
);

CREATE TABLE setup_categories
(
  setup_id VARCHAR(26) NOT NULL,
  category VARCHAR(255)
);

CREATE TABLE tag
(
  id   VARCHAR(26) NOT NULL,
  name VARCHAR(255),
  CONSTRAINT pk_tag PRIMARY KEY (id)
);

CREATE TABLE "user"
(
  id                  VARCHAR(26)  NOT NULL,
  provider_id         VARCHAR(255),
  username            VARCHAR(255) UNIQUE,
  full_name           VARCHAR(255),
  email               VARCHAR(255) UNIQUE,
  hashed_password     VARCHAR(255),
  salt                VARCHAR(255),
  profession          VARCHAR(150),
  provider            VARCHAR(255),
  created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at          TIMESTAMP WITHOUT TIME ZONE,
  email_verified_code VARCHAR(35),
  enabled             BOOLEAN,
  deleted             BOOLEAN,
  locked              BOOLEAN,
  CONSTRAINT pk_user PRIMARY KEY (id),
  CHECK ( username IS NOT NULL OR email IS NOT NULL )
);

CREATE TABLE user_preference
(
  id         VARCHAR(26) NOT NULL,
  user_id    VARCHAR(26),
  deleted    BOOLEAN,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  deleted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_user_preference PRIMARY KEY (id)
);

CREATE TABLE user_preference_preferred_categories
(
  user_preference_id   VARCHAR(26) NOT NULL,
  preferred_categories VARCHAR(255)
);

CREATE TABLE user_profile
(
  id                  VARCHAR(26) NOT NULL,
  website             VARCHAR(255),
  location            VARCHAR(255),
  bio                 VARCHAR(255),
  profile_picture_url VARCHAR(255),
  user_id             VARCHAR(26),
  CONSTRAINT pk_user_profile PRIMARY KEY (id)
);

ALTER TABLE "user"
  ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE user_preference
  ADD CONSTRAINT uc_user_preference_user UNIQUE (user_id);

ALTER TABLE user_profile
  ADD CONSTRAINT uc_user_profile_user UNIQUE (user_id);

ALTER TABLE "user"
  ADD CONSTRAINT uc_user_username UNIQUE (username);

ALTER TABLE favorite
  ADD CONSTRAINT FK_FAVORITE_ON_SETUP FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE favorite
  ADD CONSTRAINT FK_FAVORITE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE "like"
  ADD CONSTRAINT FK_LIKE_ON_SETUP FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE "like"
  ADD CONSTRAINT FK_LIKE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE setup
  ADD CONSTRAINT FK_SETUP_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE user_preference
  ADD CONSTRAINT FK_USER_PREFERENCE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE user_profile
  ADD CONSTRAINT FK_USER_PROFILE_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE setup_images
  ADD CONSTRAINT fk_setup_images_on_setup FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE setup_tag
  ADD CONSTRAINT fk_setup_tag_on_setup FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE setup_tag
  ADD CONSTRAINT fk_setup_tag_on_tag FOREIGN KEY (tag_id) REFERENCES tag (id);

ALTER TABLE setup_videos
  ADD CONSTRAINT fk_setup_videos_on_setup FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE setup_categories
  ADD CONSTRAINT fk_setup_categories_on_setup FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE user_preference_preferred_categories
  ADD CONSTRAINT fk_userpreference_preferredcategories_on_user_preference FOREIGN KEY (user_preference_id) REFERENCES user_preference (id);

ALTER TABLE comment
  ADD CONSTRAINT FK_COMMENT_ON_PARENT FOREIGN KEY (parent_id) REFERENCES comment (id);

ALTER TABLE comment
  ADD CONSTRAINT FK_COMMENT_ON_SETUP FOREIGN KEY (setup_id) REFERENCES setup (id);

ALTER TABLE comment
  ADD CONSTRAINT FK_COMMENT_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

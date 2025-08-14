CREATE TABLE system_requirement_categories
(
  system_requirement_id VARCHAR(26) NOT NULL,
  categories            VARCHAR(255)
);

CREATE TABLE system_requirement_images
(
  system_requirement_id VARCHAR(26) NOT NULL,
  images                VARCHAR(255)
);

ALTER TABLE system_requirement_categories
  ADD CONSTRAINT fk_systemrequirement_categories_on_system_requirement FOREIGN KEY (system_requirement_id) REFERENCES system_requirement (id);

ALTER TABLE system_requirement_images
  ADD CONSTRAINT fk_systemrequirement_images_on_system_requirement FOREIGN KEY (system_requirement_id) REFERENCES system_requirement (id);

CREATE TABLE tag (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  slug VARCHAR(100) NOT NULL,
  CONSTRAINT uq_tag_name UNIQUE (name),
  CONSTRAINT uq_tag_slug UNIQUE (slug)
);

CREATE TABLE report_tag (
  report_definition_id UUID NOT NULL,
  tag_id UUID NOT NULL,
  PRIMARY KEY (report_definition_id, tag_id),
  CONSTRAINT fk_report_tag_report
      FOREIGN KEY (report_definition_id) REFERENCES report_definition (id) ON DELETE CASCADE,
  CONSTRAINT fk_report_tag_tag
      FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE
);

CREATE INDEX idx_report_tag_tag ON report_tag (tag_id);

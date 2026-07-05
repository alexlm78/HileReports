ALTER TABLE report_definition
  ADD COLUMN updated_at TIMESTAMPTZ,
  ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE report_version
  ADD COLUMN updated_at TIMESTAMPTZ,
  ADD COLUMN updated_by VARCHAR(100);

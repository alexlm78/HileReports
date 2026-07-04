CREATE TABLE data_source (
  id UUID PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  db_type VARCHAR(20) NOT NULL,
  host VARCHAR(255) NOT NULL,
  port INTEGER NOT NULL,
  database_or_service VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL,
  secret_ref VARCHAR(255) NOT NULL,
  ssl_mode VARCHAR(50),
  status VARCHAR(20) NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_data_source_name UNIQUE (name),
  CONSTRAINT chk_data_source_db_type CHECK (db_type IN ('ORACLE', 'MYSQL', 'POSTGRESQL'))
);

CREATE TABLE category (
  id UUID PRIMARY KEY,
  name VARCHAR(120) NOT NULL,
  description TEXT,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TABLE report_definition (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  description TEXT,
  category_id UUID,
  data_source_id UUID NOT NULL,
  owner_team VARCHAR(120),
  status VARCHAR(20) NOT NULL,
  current_version_id UUID,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_definition_category
      FOREIGN KEY (category_id) REFERENCES category (id),
  CONSTRAINT fk_report_definition_data_source
      FOREIGN KEY (data_source_id) REFERENCES data_source (id),
  CONSTRAINT chk_report_definition_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE report_version (
  id UUID PRIMARY KEY,
  report_definition_id UUID NOT NULL,
  version_number INTEGER NOT NULL,
  sql_text TEXT NOT NULL,
  sql_hash VARCHAR(128) NOT NULL,
  validation_status VARCHAR(30) NOT NULL,
  preview_status VARCHAR(30) NOT NULL,
  max_rows INTEGER NOT NULL,
  timeout_seconds INTEGER NOT NULL,
  execution_mode VARCHAR(20) NOT NULL,
  created_by VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_version_report_definition
      FOREIGN KEY (report_definition_id) REFERENCES report_definition (id),
  CONSTRAINT uq_report_version_number UNIQUE (report_definition_id, version_number),
  CONSTRAINT chk_report_version_execution_mode CHECK (execution_mode IN ('SYNC', 'ASYNC', 'AUTO'))
);

ALTER TABLE report_definition
  ADD CONSTRAINT fk_report_definition_current_version
  FOREIGN KEY (current_version_id) REFERENCES report_version (id);

CREATE TABLE report_column (
  id UUID PRIMARY KEY,
  report_version_id UUID NOT NULL,
  source_name VARCHAR(255) NOT NULL,
  label VARCHAR(255) NOT NULL,
  data_type VARCHAR(100) NOT NULL,
  display_type VARCHAR(100),
  display_format VARCHAR(100),
  ordinal INTEGER NOT NULL,
  is_visible BOOLEAN NOT NULL DEFAULT TRUE,
  is_sortable BOOLEAN NOT NULL DEFAULT FALSE,
  is_filterable_candidate BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_report_column_report_version
      FOREIGN KEY (report_version_id) REFERENCES report_version (id),
  CONSTRAINT uq_report_column_source UNIQUE (report_version_id, source_name),
  CONSTRAINT uq_report_column_ordinal UNIQUE (report_version_id, ordinal)
);

CREATE TABLE report_parameter (
  id UUID PRIMARY KEY,
  report_version_id UUID NOT NULL,
  name VARCHAR(255) NOT NULL,
  label VARCHAR(255) NOT NULL,
  parameter_type VARCHAR(100) NOT NULL,
  operator_type VARCHAR(100) NOT NULL,
  required BOOLEAN NOT NULL DEFAULT FALSE,
  default_value TEXT,
  allows_multiple BOOLEAN NOT NULL DEFAULT FALSE,
  source_column VARCHAR(255),
  validation_rule TEXT,
  CONSTRAINT fk_report_parameter_report_version
      FOREIGN KEY (report_version_id) REFERENCES report_version (id),
  CONSTRAINT uq_report_parameter_name UNIQUE (report_version_id, name)
);

CREATE TABLE report_execution (
  id UUID PRIMARY KEY,
  report_definition_id UUID NOT NULL,
  report_version_id UUID NOT NULL,
  requested_by VARCHAR(100) NOT NULL,
  requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status VARCHAR(30) NOT NULL,
  execution_mode VARCHAR(20) NOT NULL,
  row_count BIGINT,
  duration_ms BIGINT,
  error_code VARCHAR(100),
  error_message_sanitized TEXT,
  correlation_id VARCHAR(100) NOT NULL,
  CONSTRAINT fk_report_execution_report_definition
      FOREIGN KEY (report_definition_id) REFERENCES report_definition (id),
  CONSTRAINT fk_report_execution_report_version
      FOREIGN KEY (report_version_id) REFERENCES report_version (id),
  CONSTRAINT chk_report_execution_mode CHECK (execution_mode IN ('SYNC', 'ASYNC', 'AUTO'))
);

CREATE TABLE report_execution_parameter (
  execution_id UUID NOT NULL,
  parameter_name VARCHAR(255) NOT NULL,
  parameter_value_masked TEXT,
  PRIMARY KEY (execution_id, parameter_name),
  CONSTRAINT fk_report_execution_parameter_execution
      FOREIGN KEY (execution_id) REFERENCES report_execution (id)
);

CREATE TABLE report_export (
  id UUID PRIMARY KEY,
  execution_id UUID NOT NULL,
  format VARCHAR(20) NOT NULL,
  storage_path TEXT NOT NULL,
  status VARCHAR(30) NOT NULL,
  expires_at TIMESTAMPTZ,
  CONSTRAINT fk_report_export_execution
      FOREIGN KEY (execution_id) REFERENCES report_execution (id),
  CONSTRAINT chk_report_export_format CHECK (format IN ('CSV', 'XLSX'))
);

CREATE TABLE audit_event (
  id UUID PRIMARY KEY,
  actor VARCHAR(100) NOT NULL,
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(100) NOT NULL,
  entity_id UUID,
  payload_json JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_report_definition_status ON report_definition (status);
CREATE INDEX idx_report_version_definition ON report_version (report_definition_id);
CREATE INDEX idx_report_execution_requested_at ON report_execution (requested_at);
CREATE INDEX idx_report_execution_correlation_id ON report_execution (correlation_id);
CREATE INDEX idx_audit_event_created_at ON audit_event (created_at);

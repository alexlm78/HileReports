CREATE TABLE datasource_access (
  datasource_id UUID NOT NULL,
  user_id UUID NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT pk_datasource_access PRIMARY KEY (datasource_id, user_id),
  CONSTRAINT fk_dsa_datasource FOREIGN KEY (datasource_id) REFERENCES data_source (id) ON DELETE CASCADE,
  CONSTRAINT fk_dsa_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE
);

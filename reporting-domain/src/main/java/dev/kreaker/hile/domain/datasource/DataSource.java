package dev.kreaker.hile.domain.datasource;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DataSource(
    UUID id,
    String name,
    DataSourceType dbType,
    String host,
    int port,
    String databaseOrService,
    String username,
    String secretRef,
    String sslMode,
    DataSourceStatus status,
    String createdBy,
    OffsetDateTime createdAt) {}

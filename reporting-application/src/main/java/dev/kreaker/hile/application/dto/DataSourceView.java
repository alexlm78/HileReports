package dev.kreaker.hile.application.dto;

import dev.kreaker.hile.domain.datasource.DataSourceStatus;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DataSourceView(
    UUID id,
    String name,
    DataSourceType dbType,
    String host,
    int port,
    String databaseOrService,
    String username,
    String sslMode,
    DataSourceStatus status,
    String createdBy,
    OffsetDateTime createdAt) {}

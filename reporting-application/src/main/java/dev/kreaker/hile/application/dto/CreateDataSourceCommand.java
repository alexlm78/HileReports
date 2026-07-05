package dev.kreaker.hile.application.dto;

import dev.kreaker.hile.domain.datasource.DataSourceType;

public record CreateDataSourceCommand(
    String name,
    DataSourceType dbType,
    String host,
    int port,
    String databaseOrService,
    String username,
    String rawPassword,
    String sslMode,
    String createdBy) {}

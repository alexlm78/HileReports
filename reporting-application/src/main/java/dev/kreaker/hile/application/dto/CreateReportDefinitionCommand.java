package dev.kreaker.hile.application.dto;

import dev.kreaker.hile.domain.datasource.DataSourceType;

public record CreateReportDefinitionCommand(
    String name, String description, DataSourceType dataSourceType, String sqlText) {}

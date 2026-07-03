package com.hile.reports.application.dto;

import com.hile.reports.domain.datasource.DataSourceType;

public record CreateReportDefinitionCommand(
    String name, String description, DataSourceType dataSourceType, String sqlText) {}

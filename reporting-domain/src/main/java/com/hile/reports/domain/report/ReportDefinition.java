package com.hile.reports.domain.report;

import com.hile.reports.domain.datasource.DataSourceType;
import java.util.UUID;

public record ReportDefinition(
    UUID id, String name, String description, DataSourceType dataSourceType, ReportStatus status) {}

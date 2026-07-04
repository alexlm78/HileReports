package dev.kreaker.hile.domain.report;

import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.util.UUID;

public record ReportDefinition(
    UUID id, String name, String description, DataSourceType dataSourceType, ReportStatus status) {}

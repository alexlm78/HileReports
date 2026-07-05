package dev.kreaker.hile.application.dto;

import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportDefinitionView(
    UUID id,
    String name,
    String description,
    UUID dataSourceId,
    String ownerTeam,
    ReportStatus status,
    String sqlText,
    String createdBy,
    OffsetDateTime createdAt) {}

package dev.kreaker.hile.domain.report;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportDefinition(
    UUID id,
    String name,
    String description,
    UUID dataSourceId,
    String ownerTeam,
    ReportStatus status,
    String sqlText,
    String previewStatus,
    UUID currentVersionId,
    String createdBy,
    OffsetDateTime createdAt) {}

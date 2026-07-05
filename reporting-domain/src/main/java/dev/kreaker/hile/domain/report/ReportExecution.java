package dev.kreaker.hile.domain.report;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportExecution(
    UUID id,
    UUID reportDefinitionId,
    UUID reportVersionId,
    String requestedBy,
    OffsetDateTime requestedAt,
    String status,
    String executionMode,
    Long rowCount,
    Long durationMs,
    String errorCode,
    String errorMessageSanitized,
    String correlationId) {}

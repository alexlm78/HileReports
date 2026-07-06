package dev.kreaker.hile.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExecutionView(
    UUID id,
    UUID reportDefinitionId,
    String requestedBy,
    OffsetDateTime requestedAt,
    String status,
    String executionMode,
    Long rowCount,
    Long durationMs,
    String errorCode,
    String correlationId) {}

package dev.kreaker.hile.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditEventView(
    UUID id,
    String actor,
    String action,
    String entityType,
    UUID entityId,
    String payloadJson,
    OffsetDateTime createdAt) {}

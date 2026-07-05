package dev.kreaker.hile.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExportJobView(
    UUID id, UUID executionId, String format, String status, OffsetDateTime expiresAt) {}

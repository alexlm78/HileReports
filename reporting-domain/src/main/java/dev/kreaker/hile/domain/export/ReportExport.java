package dev.kreaker.hile.domain.export;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportExport(
    UUID id,
    UUID executionId,
    String format,
    String storagePath,
    String status,
    OffsetDateTime expiresAt) {}

package dev.kreaker.hile.domain.report;

import java.util.UUID;

public record ReportColumn(
    UUID id,
    UUID reportVersionId,
    String sourceName,
    String label,
    String dataType,
    String displayType,
    String displayFormat,
    int ordinal,
    boolean visible,
    boolean sortable,
    boolean filterableCandidate) {}

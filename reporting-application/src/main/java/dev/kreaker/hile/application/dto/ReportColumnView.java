package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record ReportColumnView(
    UUID id,
    String sourceName,
    String label,
    String dataType,
    String displayType,
    String displayFormat,
    int ordinal,
    boolean visible,
    boolean sortable,
    boolean filterableCandidate) {}

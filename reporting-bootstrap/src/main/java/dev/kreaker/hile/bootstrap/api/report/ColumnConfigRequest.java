package dev.kreaker.hile.bootstrap.api.report;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ColumnConfigRequest(
    @NotBlank String sourceName,
    @NotBlank String dataType,
    @NotBlank String label,
    String displayType,
    String displayFormat,
    @Min(0) int ordinal,
    boolean visible,
    boolean sortable,
    boolean filterableCandidate) {}

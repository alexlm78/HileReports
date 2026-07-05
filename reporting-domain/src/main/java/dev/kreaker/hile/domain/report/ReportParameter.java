package dev.kreaker.hile.domain.report;

import java.util.UUID;

public record ReportParameter(
    UUID id,
    UUID reportVersionId,
    String name,
    String label,
    String parameterType,
    String operatorType,
    boolean required,
    String defaultValue,
    boolean allowsMultiple,
    String sourceColumn,
    String validationRule) {}

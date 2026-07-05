package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record ReportParameterView(
    UUID id,
    String name,
    String label,
    String parameterType,
    String operatorType,
    boolean required,
    String defaultValue,
    boolean allowsMultiple,
    String sourceColumn,
    String validationRule) {}

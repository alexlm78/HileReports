package dev.kreaker.hile.bootstrap.api.report;

import jakarta.validation.constraints.NotBlank;

public record ParameterConfigRequest(
    @NotBlank String name,
    @NotBlank String label,
    @NotBlank String parameterType,
    @NotBlank String operatorType,
    boolean required,
    String defaultValue,
    boolean allowsMultiple,
    String sourceColumn,
    String validationRule) {}

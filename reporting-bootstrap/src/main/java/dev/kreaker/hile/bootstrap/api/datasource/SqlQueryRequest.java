package dev.kreaker.hile.bootstrap.api.datasource;

import jakarta.validation.constraints.NotBlank;

public record SqlQueryRequest(@NotBlank String sqlText) {}

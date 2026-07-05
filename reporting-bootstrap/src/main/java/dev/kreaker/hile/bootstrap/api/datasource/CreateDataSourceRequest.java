package dev.kreaker.hile.bootstrap.api.datasource;

import dev.kreaker.hile.domain.datasource.DataSourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDataSourceRequest(
    @NotBlank String name,
    @NotNull DataSourceType dbType,
    @NotBlank String host,
    @Min(1) @Max(65535) int port,
    @NotBlank String databaseOrService,
    @NotBlank String username,
    @NotBlank String password,
    String sslMode) {}

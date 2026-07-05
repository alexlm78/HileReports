package dev.kreaker.hile.bootstrap.api.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReportRequest(
    @NotBlank String name,
    String description,
    @NotNull UUID dataSourceId,
    String ownerTeam,
    @NotBlank String sqlText) {}

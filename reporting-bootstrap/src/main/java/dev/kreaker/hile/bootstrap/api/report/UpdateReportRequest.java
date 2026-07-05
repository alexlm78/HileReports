package dev.kreaker.hile.bootstrap.api.report;

import java.util.UUID;

public record UpdateReportRequest(
    String name,
    String description,
    UUID categoryId,
    UUID dataSourceId,
    String ownerTeam,
    String sqlText) {}

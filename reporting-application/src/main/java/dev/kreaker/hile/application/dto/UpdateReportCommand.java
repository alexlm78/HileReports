package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record UpdateReportCommand(
    UUID id,
    String name,
    String description,
    UUID categoryId,
    UUID dataSourceId,
    String ownerTeam,
    String sqlText) {}

package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record CreateReportDefinitionCommand(
    String name,
    String description,
    UUID categoryId,
    UUID dataSourceId,
    String ownerTeam,
    String sqlText,
    String createdBy) {}

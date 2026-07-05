package dev.kreaker.hile.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CatalogReportView(
    UUID id,
    String name,
    String description,
    UUID dataSourceId,
    String ownerTeam,
    String createdBy,
    OffsetDateTime createdAt) {}

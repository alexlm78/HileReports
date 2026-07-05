package dev.kreaker.hile.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CategoryView(
    UUID id, String name, String description, String createdBy, OffsetDateTime createdAt) {}

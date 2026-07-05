package dev.kreaker.hile.domain.category;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Category(
    UUID id, String name, String description, String createdBy, OffsetDateTime createdAt) {}

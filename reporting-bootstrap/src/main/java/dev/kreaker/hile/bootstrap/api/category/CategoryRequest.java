package dev.kreaker.hile.bootstrap.api.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(@NotBlank String name, String description) {}

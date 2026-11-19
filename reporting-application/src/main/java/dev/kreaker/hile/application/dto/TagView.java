package dev.kreaker.hile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.UUID;

public record TagView(@Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
      @Schema(requiredMode = RequiredMode.REQUIRED) String name,
      @Schema(requiredMode = RequiredMode.REQUIRED) String slug) {
}

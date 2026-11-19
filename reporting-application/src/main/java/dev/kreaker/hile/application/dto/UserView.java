package dev.kreaker.hile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

import java.util.Set;
import java.util.UUID;

public record UserView(@Schema(requiredMode = RequiredMode.REQUIRED) UUID id,
      @Schema(requiredMode = RequiredMode.REQUIRED) String username,
      @Schema(requiredMode = RequiredMode.REQUIRED, types = {
            "string", "null"}) String email,
      @Schema(requiredMode = RequiredMode.REQUIRED) Set<String> roles,
      @Schema(requiredMode = RequiredMode.REQUIRED) boolean enabled) {
}

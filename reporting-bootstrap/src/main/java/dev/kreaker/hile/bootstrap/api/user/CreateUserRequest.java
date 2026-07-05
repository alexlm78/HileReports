package dev.kreaker.hile.bootstrap.api.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Size(min = 8, max = 100) String password,
    String email,
    @NotBlank
        @Pattern(
            regexp = "platform_admin|report_designer|report_viewer",
            message = "must be platform_admin, report_designer, or report_viewer")
        String role) {}

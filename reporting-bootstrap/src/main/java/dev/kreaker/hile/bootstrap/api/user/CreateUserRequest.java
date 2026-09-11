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
            regexp = "PLATFORM_ADMIN|REPORT_DESIGNER|REPORT_VIEWER",
            message = "must be PLATFORM_ADMIN, REPORT_DESIGNER, or REPORT_VIEWER")
        String role) {}

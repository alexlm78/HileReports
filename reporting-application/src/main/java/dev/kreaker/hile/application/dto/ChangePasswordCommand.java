package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record ChangePasswordCommand(UUID userId, String newPassword) {}

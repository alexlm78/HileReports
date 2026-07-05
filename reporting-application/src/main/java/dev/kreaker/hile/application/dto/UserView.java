package dev.kreaker.hile.application.dto;

import java.util.Set;
import java.util.UUID;

public record UserView(
    UUID id, String username, String email, Set<String> roles, boolean enabled) {}

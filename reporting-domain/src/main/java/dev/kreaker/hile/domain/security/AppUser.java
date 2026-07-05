package dev.kreaker.hile.domain.security;

import java.util.Set;
import java.util.UUID;

public record AppUser(UUID id, String username, String email, Set<String> roles, boolean enabled) {}

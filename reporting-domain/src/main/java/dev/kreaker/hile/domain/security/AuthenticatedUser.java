package dev.kreaker.hile.domain.security;

import java.util.Set;

public record AuthenticatedUser(String username, Set<String> roles) {}

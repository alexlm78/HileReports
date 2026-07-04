package dev.kreaker.hile.domain.security;

import java.util.Set;

public record UserCredentials(String username, String passwordHash, Set<String> roles) {}

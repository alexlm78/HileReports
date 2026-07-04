package dev.kreaker.hile.bootstrap.api.auth;

import java.util.Set;

public record LoginResponse(String username, Set<String> roles, String authenticationMode) {}

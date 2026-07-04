package dev.kreaker.hile.application.dto;

import java.util.Set;

public record AuthenticationResult(String username, Set<String> roles) {}

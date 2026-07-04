package dev.kreaker.hile.security;

import java.util.Set;

public interface TokenProviderPort {

  String generateToken(String username, Set<String> roles);

  long expirationMs();

  boolean isValid(String token);

  String extractUsername(String token);

  Set<String> extractRoles(String token);
}

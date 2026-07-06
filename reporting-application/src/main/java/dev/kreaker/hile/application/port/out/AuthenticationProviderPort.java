package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.domain.security.AuthenticatedUser;

public interface AuthenticationProviderPort {

  AuthenticatedUser authenticate(String username, String rawPassword);
}

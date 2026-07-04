package dev.kreaker.hile.security;

import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.domain.security.AuthenticatedUser;

public class AdAuthenticationProviderStub implements AuthenticationProviderPort {

  @Override
  public AuthenticatedUser authenticate(String username, String rawPassword) {
    throw new UnsupportedOperationException(
        "La integracion con Active Directory se implementara en una fase posterior.");
  }
}

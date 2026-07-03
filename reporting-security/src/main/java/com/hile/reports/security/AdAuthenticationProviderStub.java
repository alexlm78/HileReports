package com.hile.reports.security;

import com.hile.reports.application.port.out.AuthenticationProviderPort;
import com.hile.reports.domain.security.AuthenticatedUser;

public class AdAuthenticationProviderStub implements AuthenticationProviderPort {

  @Override
  public AuthenticatedUser authenticate(String username, String rawPassword) {
    throw new UnsupportedOperationException(
        "La integracion con Active Directory se implementara en una fase posterior.");
  }
}

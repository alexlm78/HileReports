package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.AuthenticateUserCommand;
import dev.kreaker.hile.application.dto.AuthenticationResult;
import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.domain.security.AuthenticatedUser;
import java.util.Set;

public class AuthenticationApplicationService implements AuthenticateUserUseCase {

  private final AuthenticationProviderPort authenticationProviderPort;

  public AuthenticationApplicationService(AuthenticationProviderPort authenticationProviderPort) {
    this.authenticationProviderPort = authenticationProviderPort;
  }

  @Override
  public AuthenticationResult authenticate(AuthenticateUserCommand command) {
    if (command.username() == null || command.username().isBlank()) {
      throw new InvalidCredentialsException("El usuario es obligatorio.");
    }
    if (command.password() == null || command.password().isBlank()) {
      throw new InvalidCredentialsException("La contrasena es obligatoria.");
    }

    AuthenticatedUser authenticatedUser =
        authenticationProviderPort.authenticate(command.username(), command.password());
    return new AuthenticationResult(
        authenticatedUser.username(), Set.copyOf(authenticatedUser.roles()));
  }
}

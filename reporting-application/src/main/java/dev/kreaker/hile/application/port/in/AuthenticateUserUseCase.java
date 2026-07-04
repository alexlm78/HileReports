package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.AuthenticateUserCommand;
import dev.kreaker.hile.application.dto.AuthenticationResult;

public interface AuthenticateUserUseCase {

  AuthenticationResult authenticate(AuthenticateUserCommand command);
}

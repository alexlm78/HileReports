package dev.kreaker.hile.security;

import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import dev.kreaker.hile.domain.security.AuthenticatedUser;
import dev.kreaker.hile.domain.security.UserCredentials;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalAuthenticationProviderAdapter implements AuthenticationProviderPort {

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public LocalAuthenticationProviderAdapter(UserRepositoryPort userRepositoryPort) {
    this.userRepositoryPort = userRepositoryPort;
  }

  @Override
  public AuthenticatedUser authenticate(String username, String rawPassword) {
    UserCredentials credentials =
        userRepositoryPort
            .findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas."));

    if (!passwordEncoder.matches(rawPassword, credentials.passwordHash())) {
      throw new InvalidCredentialsException("Credenciales invalidas.");
    }

    return new AuthenticatedUser(username, credentials.roles());
  }
}

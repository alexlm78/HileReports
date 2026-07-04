package dev.kreaker.hile.security;

import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.domain.security.AuthenticatedUser;
import java.util.Map;
import java.util.Set;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalAuthenticationProviderAdapter implements AuthenticationProviderPort {

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final Map<String, String> localUsers =
      Map.of("admin", passwordEncoder.encode("admin123"));

  @Override
  public AuthenticatedUser authenticate(String username, String rawPassword) {
    String storedPassword = localUsers.get(username);
    if (storedPassword == null || !passwordEncoder.matches(rawPassword, storedPassword)) {
      throw new IllegalArgumentException("Credenciales invalidas.");
    }
    return new AuthenticatedUser(username, Set.of("platform_admin"));
  }
}

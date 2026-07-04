package dev.kreaker.hile.bootstrap.config;

import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import dev.kreaker.hile.application.service.AuthenticationApplicationService;
import dev.kreaker.hile.security.AdAuthenticationProviderStub;
import dev.kreaker.hile.security.LocalAuthenticationProviderAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationWiringConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationProviderPort authenticationProviderPort(
      @Value("${hile.reports.security.mode:local}") String securityMode,
      UserRepositoryPort userRepositoryPort) {
    if ("ad".equalsIgnoreCase(securityMode)) {
      return new AdAuthenticationProviderStub();
    }
    return new LocalAuthenticationProviderAdapter(userRepositoryPort);
  }

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      AuthenticationProviderPort authenticationProviderPort) {
    return new AuthenticationApplicationService(authenticationProviderPort);
  }
}

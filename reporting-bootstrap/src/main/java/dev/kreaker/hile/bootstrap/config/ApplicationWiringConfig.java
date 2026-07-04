package dev.kreaker.hile.bootstrap.config;

import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.application.service.AuthenticationApplicationService;
import dev.kreaker.hile.security.AdAuthenticationProviderStub;
import dev.kreaker.hile.security.LocalAuthenticationProviderAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWiringConfig {

  @Bean
  AuthenticationProviderPort authenticationProviderPort(
      @Value("${hile.reports.security.mode:local}") String securityMode) {
    if ("ad".equalsIgnoreCase(securityMode)) {
      return new AdAuthenticationProviderStub();
    }
    return new LocalAuthenticationProviderAdapter();
  }

  @Bean
  AuthenticateUserUseCase authenticateUserUseCase(
      AuthenticationProviderPort authenticationProviderPort) {
    return new AuthenticationApplicationService(authenticationProviderPort);
  }
}

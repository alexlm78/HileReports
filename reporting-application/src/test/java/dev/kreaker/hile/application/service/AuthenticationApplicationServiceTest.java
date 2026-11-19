package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.AuthenticateUserCommand;
import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import dev.kreaker.hile.application.port.out.AuthenticationProviderPort;
import dev.kreaker.hile.domain.security.AuthenticatedUser;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationApplicationServiceTest {

   @Test
   void shouldAuthenticateUsingConfiguredProvider() {
      AuthenticationProviderPort authenticationProviderPort =
            (username, password) -> new AuthenticatedUser(username, Set.of("platform_admin"));
      AuthenticationApplicationService service =
            new AuthenticationApplicationService(authenticationProviderPort);

      var result = service.authenticate(new AuthenticateUserCommand("admin", "admin123"));

      assertEquals("admin", result.username());
      assertEquals(Set.of("platform_admin"), result.roles());
   }

   @Test
   void shouldRejectBlankUsername() {
      AuthenticationProviderPort authenticationProviderPort =
            (username, password) -> new AuthenticatedUser(username, Set.of("platform_admin"));
      AuthenticationApplicationService service =
            new AuthenticationApplicationService(authenticationProviderPort);

      assertThrows(InvalidCredentialsException.class,
            () -> service.authenticate(new AuthenticateUserCommand(" ", "admin123")));
   }
}

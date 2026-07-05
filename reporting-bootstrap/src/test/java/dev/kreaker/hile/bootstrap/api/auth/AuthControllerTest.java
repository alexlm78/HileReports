package dev.kreaker.hile.bootstrap.api.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.kreaker.hile.application.dto.AuthenticationResult;
import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.bootstrap.api.ArchitectureController;
import dev.kreaker.hile.bootstrap.api.GlobalExceptionHandler;
import dev.kreaker.hile.bootstrap.config.SecurityConfig;
import dev.kreaker.hile.security.TokenProviderPort;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {AuthController.class, ArchitectureController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AuthenticateUserUseCase authenticateUserUseCase;
  @MockBean private TokenProviderPort jwtTokenProvider;
  @MockBean private AuditEventPort auditEventPort;

  @Test
  void shouldAuthenticateWithValidCredentials() throws Exception {
    given(authenticateUserUseCase.authenticate(any()))
        .willReturn(new AuthenticationResult("admin", Set.of("platform_admin")));
    given(jwtTokenProvider.generateToken(anyString(), anySet())).willReturn("test.jwt.token");
    given(jwtTokenProvider.expirationMs()).willReturn(86400000L);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "admin",
                      "password": "admin123"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin"))
        .andExpect(jsonPath("$.roles[0]").value("platform_admin"))
        .andExpect(jsonPath("$.token").value("test.jwt.token"))
        .andExpect(jsonPath("$.expiresInMs").value(86400000))
        .andExpect(jsonPath("$.authenticationMode").value("local"));
  }

  @Test
  void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
    given(authenticateUserUseCase.authenticate(any()))
        .willThrow(new InvalidCredentialsException("Credenciales invalidas."));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "admin",
                      "password": "wrong"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
  }

  @Test
  void shouldProtectOtherEndpointsWhenRequestIsAnonymous() throws Exception {
    mockMvc.perform(get("/api/v1/architecture/modules")).andExpect(status().isUnauthorized());
  }
}

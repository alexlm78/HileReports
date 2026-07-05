package dev.kreaker.hile.bootstrap.api.auth;

import dev.kreaker.hile.application.dto.AuthenticateUserCommand;
import dev.kreaker.hile.application.dto.AuthenticationResult;
import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.security.TokenProviderPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final TokenProviderPort jwtTokenProvider;
  private final AuditEventPort auditEventPort;
  private final String authenticationMode;

  public AuthController(
      AuthenticateUserUseCase authenticateUserUseCase,
      TokenProviderPort jwtTokenProvider,
      AuditEventPort auditEventPort,
      @Value("${hile.reports.security.mode:local}") String authenticationMode) {
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.jwtTokenProvider = jwtTokenProvider;
    this.auditEventPort = auditEventPort;
    this.authenticationMode = authenticationMode;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationResult result =
        authenticateUserUseCase.authenticate(
            new AuthenticateUserCommand(request.username(), request.password()));

    String token = jwtTokenProvider.generateToken(result.username(), result.roles());

    auditEventPort.record(result.username(), "USER_LOGIN", "USER", null);

    return ResponseEntity.ok(
        new LoginResponse(
            result.username(),
            result.roles(),
            token,
            jwtTokenProvider.expirationMs(),
            authenticationMode));
  }
}

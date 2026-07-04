package dev.kreaker.hile.bootstrap.api.auth;

import dev.kreaker.hile.application.dto.AuthenticateUserCommand;
import dev.kreaker.hile.application.dto.AuthenticationResult;
import dev.kreaker.hile.application.port.in.AuthenticateUserUseCase;
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
  private final String authenticationMode;

  public AuthController(
      AuthenticateUserUseCase authenticateUserUseCase,
      @Value("${hile.reports.security.mode:local}") String authenticationMode) {
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.authenticationMode = authenticationMode;
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthenticationResult result =
        authenticateUserUseCase.authenticate(
            new AuthenticateUserCommand(request.username(), request.password()));

    return ResponseEntity.ok(
        new LoginResponse(result.username(), result.roles(), authenticationMode));
  }
}

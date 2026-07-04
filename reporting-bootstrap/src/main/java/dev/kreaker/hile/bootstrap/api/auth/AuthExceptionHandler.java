package dev.kreaker.hile.bootstrap.api.auth;

import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
      InvalidCredentialsException invalidCredentialsException) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ApiErrorResponse("AUTH_INVALID_CREDENTIALS", invalidCredentialsException.getMessage()));
  }
}

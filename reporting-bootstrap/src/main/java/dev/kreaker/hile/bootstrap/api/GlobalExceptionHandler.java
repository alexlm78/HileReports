package dev.kreaker.hile.bootstrap.api;

import dev.kreaker.hile.application.exception.AccessDeniedException;
import dev.kreaker.hile.application.exception.DataSourceNotFoundException;
import dev.kreaker.hile.application.exception.InvalidCredentialsException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiErrorResponse.of("AUTH_INVALID_CREDENTIALS", ex.getMessage()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  ApiErrorResponse handleAccessDenied(AccessDeniedException ex) {
    return ApiErrorResponse.of("ACCESS_DENIED", ex.getMessage());
  }

  @ExceptionHandler(DataSourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiErrorResponse handleDataSourceNotFound(DataSourceNotFoundException ex) {
    return ApiErrorResponse.of("DATASOURCE_NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  ApiErrorResponse handleNotFound(IllegalArgumentException ex) {
    return ApiErrorResponse.of("NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  ApiErrorResponse handleConflict(IllegalStateException ex) {
    return ApiErrorResponse.of("CONFLICT", ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ApiErrorResponse handleValidation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return ApiErrorResponse.of(
        "VALIDATION_ERROR", message.isEmpty() ? "Validation failed" : message);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  ApiErrorResponse handleUnexpected(Exception ex) {
    return ApiErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred");
  }
}

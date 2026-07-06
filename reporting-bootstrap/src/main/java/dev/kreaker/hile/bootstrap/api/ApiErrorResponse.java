package dev.kreaker.hile.bootstrap.api;

import java.time.OffsetDateTime;

public record ApiErrorResponse(String code, String message, String timestamp) {

  static ApiErrorResponse of(String code, String message) {
    return new ApiErrorResponse(code, message, OffsetDateTime.now().toString());
  }
}

package com.hile.reports.application.dto;

import java.util.List;

public record ValidationResult(boolean valid, List<String> messages) {
  public static ValidationResult ok() {
    return new ValidationResult(true, List.of());
  }

  public static ValidationResult failure(String message) {
    return new ValidationResult(false, List.of(message));
  }
}

package com.hile.reports.infrastructure.sql;

import com.hile.reports.application.dto.ValidationResult;
import com.hile.reports.application.port.out.QueryValidatorPort;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleReadOnlyQueryValidator implements QueryValidatorPort {

  private static final Pattern NAMED_PARAMETER_PATTERN =
      Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");

  @Override
  public ValidationResult validateReadOnly(String sqlText) {
    if (sqlText == null || sqlText.isBlank()) {
      return ValidationResult.failure("El SQL es obligatorio.");
    }

    String normalized = sqlText.toLowerCase(Locale.ROOT);
    List<String> forbiddenTokens =
        List.of("insert ", "update ", "delete ", "drop ", "alter ", "truncate ", ";");
    boolean hasForbiddenToken = forbiddenTokens.stream().anyMatch(normalized::contains);
    if (hasForbiddenToken) {
      return ValidationResult.failure("Solo se permiten consultas de lectura.");
    }

    if (!(normalized.stripLeading().startsWith("select")
        || normalized.stripLeading().startsWith("with"))) {
      return ValidationResult.failure("La consulta debe iniciar con SELECT o WITH.");
    }

    return ValidationResult.ok();
  }

  @Override
  public List<String> extractNamedParameters(String sqlText) {
    Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(sqlText);
    return matcher.results().map(result -> result.group(1)).distinct().toList();
  }
}

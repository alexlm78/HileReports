package dev.kreaker.hile.infrastructure.sql;

import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.out.QueryValidatorPort;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleReadOnlyQueryValidator implements QueryValidatorPort {

  private static final Pattern NAMED_PARAMETER_PATTERN =
      Pattern.compile(":([a-zA-Z][a-zA-Z0-9_]*)");

  private static final Pattern BLOCK_COMMENT_PATTERN =
      Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

  private static final Pattern LINE_COMMENT_PATTERN = Pattern.compile("--[^\r\n]*");

  private static final List<String> FORBIDDEN_WRITE_TOKENS =
      List.of("insert ", "update ", "delete ", "drop ", "alter ", "truncate ", ";");

  // Patterns that are never legitimate in read-only reporting SQL.
  private static final List<String> DANGEROUS_PATTERNS =
      List.of(
          "sleep(",
          "benchmark(",
          "waitfor ",
          "load_file(",
          "into outfile",
          "into dumpfile",
          "information_schema",
          "pg_catalog",
          "pg_read_file(",
          "pg_ls_dir(",
          "xp_cmdshell",
          "exec(",
          "exec ",
          "execute(",
          "execute ");

  @Override
  public ValidationResult validateReadOnly(String sqlText) {
    if (sqlText == null || sqlText.isBlank()) {
      return ValidationResult.failure("El SQL es obligatorio.");
    }

    String normalized = stripComments(sqlText).toLowerCase(Locale.ROOT);

    if (FORBIDDEN_WRITE_TOKENS.stream().anyMatch(normalized::contains)) {
      return ValidationResult.failure("Solo se permiten consultas de lectura.");
    }

    if (!(normalized.stripLeading().startsWith("select")
        || normalized.stripLeading().startsWith("with"))) {
      return ValidationResult.failure("La consulta debe iniciar con SELECT o WITH.");
    }

    if (DANGEROUS_PATTERNS.stream().anyMatch(normalized::contains)) {
      return ValidationResult.failure("La consulta contiene patrones no permitidos.");
    }

    return ValidationResult.ok();
  }

  @Override
  public List<String> extractNamedParameters(String sqlText) {
    if (sqlText == null || sqlText.isBlank()) return List.of();
    Matcher matcher = NAMED_PARAMETER_PATTERN.matcher(stripComments(sqlText));
    return matcher.results().map(r -> r.group(1)).distinct().toList();
  }

  private static String stripComments(String sql) {
    String stripped = BLOCK_COMMENT_PATTERN.matcher(sql).replaceAll(" ");
    return LINE_COMMENT_PATTERN.matcher(stripped).replaceAll(" ");
  }
}

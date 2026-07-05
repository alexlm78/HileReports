package dev.kreaker.hile.application.service;

import dev.kreaker.hile.domain.report.ReportParameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class NamedParamBinder {

  static final Pattern PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

  private NamedParamBinder() {}

  static void validateRequired(List<ReportParameter> defs, Map<String, String> values) {
    for (ReportParameter p : defs) {
      if (p.required()) {
        String val = values.get(p.name());
        if (val == null || val.isBlank()) {
          throw new IllegalArgumentException("Required parameter missing: " + p.name());
        }
      }
    }
  }

  static BoundSql bind(String sqlText, List<ReportParameter> defs, Map<String, String> values) {
    Map<String, ReportParameter> defMap =
        defs.stream().collect(Collectors.toMap(ReportParameter::name, p -> p));
    List<Object> orderedValues = new ArrayList<>();
    StringBuffer sb = new StringBuffer();
    Matcher m = PATTERN.matcher(sqlText);
    while (m.find()) {
      String name = m.group(1);
      orderedValues.add(convertValue(defMap.get(name), values.get(name)));
      m.appendReplacement(sb, "?");
    }
    m.appendTail(sb);
    return new BoundSql(sb.toString(), Collections.unmodifiableList(orderedValues));
  }

  static Object convertValue(ReportParameter def, String raw) {
    if (raw == null) return null;
    if (def == null) return raw;
    return switch (def.parameterType().toUpperCase()) {
      case "NUMBER" -> {
        try {
          yield Long.parseLong(raw);
        } catch (NumberFormatException e) {
          yield Double.parseDouble(raw);
        }
      }
      case "INTEGER" -> Long.parseLong(raw);
      case "DATE" -> java.sql.Date.valueOf(raw);
      case "TIMESTAMP" -> java.sql.Timestamp.valueOf(raw);
      case "BOOLEAN" -> Boolean.parseBoolean(raw);
      default -> raw;
    };
  }

  record BoundSql(String sql, List<Object> values) {}
}

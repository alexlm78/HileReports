package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportExecution;
import dev.kreaker.hile.domain.report.ReportParameter;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExecuteReportApplicationService implements ExecuteReportUseCase {

  private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

  private final ReportDefinitionRepository reportRepository;
  private final ReportParameterRepositoryPort parameterRepository;
  private final DataSourceUseCase dataSourceUseCase;
  private final ReportExecutionRepository executionRepository;

  public ExecuteReportApplicationService(
      ReportDefinitionRepository reportRepository,
      ReportParameterRepositoryPort parameterRepository,
      DataSourceUseCase dataSourceUseCase,
      ReportExecutionRepository executionRepository) {
    this.reportRepository = reportRepository;
    this.parameterRepository = parameterRepository;
    this.dataSourceUseCase = dataSourceUseCase;
    this.executionRepository = executionRepository;
  }

  @Override
  public ExecutionResultView execute(ExecuteReportCommand command, String requestedBy) {
    ReportDefinition def =
        reportRepository
            .findById(command.reportId())
            .orElseThrow(
                () -> new IllegalArgumentException("Report not found: " + command.reportId()));

    if (def.status() != ReportStatus.PUBLISHED) {
      throw new IllegalStateException("Only PUBLISHED reports can be executed.");
    }
    if (def.sqlText() == null) {
      throw new IllegalStateException("Report has no SQL text.");
    }
    if (def.currentVersionId() == null) {
      throw new IllegalStateException("Report has no current version.");
    }

    List<ReportParameter> paramDefs = parameterRepository.findByReportId(command.reportId());
    Map<String, String> paramValues =
        command.parameterValues() != null ? command.parameterValues() : Map.of();

    validateRequiredParams(paramDefs, paramValues);

    BoundSql bound = bindNamedParams(def.sqlText(), paramDefs, paramValues);

    int pageSize = command.pageSize() > 0 ? command.pageSize() : 100;
    int offset = command.page() * pageSize;
    String correlationId = UUID.randomUUID().toString();
    UUID executionId = UUID.randomUUID();
    long startMs = System.currentTimeMillis();

    try {
      PreviewResult result =
          dataSourceUseCase.executeWithParams(
              def.dataSourceId(), bound.sql(), bound.values(), pageSize, offset);

      long durationMs = System.currentTimeMillis() - startMs;
      long rowCount = result.rows().size();

      ReportExecution execution =
          new ReportExecution(
              executionId,
              def.id(),
              def.currentVersionId(),
              requestedBy,
              OffsetDateTime.now(),
              "COMPLETED",
              "SYNC",
              rowCount,
              durationMs,
              null,
              null,
              correlationId);
      executionRepository.save(execution, paramValues);

      return new ExecutionResultView(
          executionId,
          correlationId,
          result.columns(),
          result.rows(),
          rowCount,
          durationMs,
          command.page(),
          pageSize);

    } catch (Exception e) {
      long durationMs = System.currentTimeMillis() - startMs;
      ReportExecution failed =
          new ReportExecution(
              executionId,
              def.id(),
              def.currentVersionId(),
              requestedBy,
              OffsetDateTime.now(),
              "FAILED",
              "SYNC",
              null,
              durationMs,
              "EXECUTION_ERROR",
              sanitize(e.getMessage()),
              correlationId);
      executionRepository.save(failed, Map.of());
      throw e;
    }
  }

  private void validateRequiredParams(List<ReportParameter> paramDefs, Map<String, String> values) {
    for (ReportParameter p : paramDefs) {
      if (p.required()) {
        String val = values.get(p.name());
        if (val == null || val.isBlank()) {
          throw new IllegalArgumentException("Required parameter missing: " + p.name());
        }
      }
    }
  }

  private BoundSql bindNamedParams(
      String sqlText, List<ReportParameter> paramDefs, Map<String, String> values) {
    Map<String, ReportParameter> defMap =
        paramDefs.stream().collect(Collectors.toMap(ReportParameter::name, p -> p));

    List<Object> orderedValues = new ArrayList<>();
    StringBuffer sb = new StringBuffer();
    Matcher m = NAMED_PARAM_PATTERN.matcher(sqlText);
    while (m.find()) {
      String name = m.group(1);
      String raw = values.get(name);
      orderedValues.add(convertValue(defMap.get(name), raw));
      m.appendReplacement(sb, "?");
    }
    m.appendTail(sb);
    return new BoundSql(sb.toString(), Collections.unmodifiableList(orderedValues));
  }

  private Object convertValue(ReportParameter def, String raw) {
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

  private String sanitize(String message) {
    if (message == null) return null;
    return message.length() > 500 ? message.substring(0, 500) : message;
  }

  private record BoundSql(String sql, List<Object> values) {}
}

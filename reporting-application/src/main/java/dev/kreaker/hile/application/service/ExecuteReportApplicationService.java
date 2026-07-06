package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;
import dev.kreaker.hile.application.dto.ExecutionView;
import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import dev.kreaker.hile.application.port.out.MetricsPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportExecution;
import dev.kreaker.hile.domain.report.ReportParameter;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExecuteReportApplicationService implements ExecuteReportUseCase {

  private final ReportDefinitionRepository reportRepository;
  private final ReportParameterRepositoryPort parameterRepository;
  private final DataSourceUseCase dataSourceUseCase;
  private final ReportExecutionRepository executionRepository;
  private final MetricsPort metrics;

  public ExecuteReportApplicationService(
      ReportDefinitionRepository reportRepository,
      ReportParameterRepositoryPort parameterRepository,
      DataSourceUseCase dataSourceUseCase,
      ReportExecutionRepository executionRepository,
      MetricsPort metrics) {
    this.reportRepository = reportRepository;
    this.parameterRepository = parameterRepository;
    this.dataSourceUseCase = dataSourceUseCase;
    this.executionRepository = executionRepository;
    this.metrics = metrics;
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

    NamedParamBinder.validateRequired(paramDefs, paramValues);
    NamedParamBinder.BoundSql bound = NamedParamBinder.bind(def.sqlText(), paramDefs, paramValues);

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
      metrics.recordExecution("COMPLETED", durationMs, rowCount);

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
      metrics.recordExecution("FAILED", durationMs, -1);
      throw e;
    }
  }

  @Override
  public PageResult<ExecutionView> listByReport(
      UUID reportId, String requestedBy, int page, int size) {
    reportRepository
        .findById(reportId)
        .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    PageResult<ReportExecution> result = executionRepository.findByReportId(reportId, page, size);
    List<ExecutionView> content = result.content().stream().map(this::toView).toList();
    return new PageResult<>(content, page, size, result.total());
  }

  @Override
  public PageResult<ExecutionView> listMine(String requestedBy, int page, int size) {
    PageResult<ReportExecution> result =
        executionRepository.findByRequestedBy(requestedBy, page, size);
    List<ExecutionView> content = result.content().stream().map(this::toView).toList();
    return new PageResult<>(content, page, size, result.total());
  }

  private ExecutionView toView(ReportExecution e) {
    return new ExecutionView(
        e.id(),
        e.reportDefinitionId(),
        e.requestedBy(),
        e.requestedAt(),
        e.status(),
        e.executionMode(),
        e.rowCount(),
        e.durationMs(),
        e.errorCode(),
        e.correlationId());
  }

  private String sanitize(String message) {
    if (message == null) return null;
    return message.length() > 500 ? message.substring(0, 500) : message;
  }
}

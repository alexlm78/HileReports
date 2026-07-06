package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.AsyncExportTask;
import dev.kreaker.hile.application.dto.ExportJobCreationResult;
import dev.kreaker.hile.application.dto.ExportJobView;
import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.RequestExportCommand;
import dev.kreaker.hile.application.exception.AccessDeniedException;
import dev.kreaker.hile.application.port.in.ExportJobUseCase;
import dev.kreaker.hile.application.port.out.DataSourceAccessPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.application.port.out.ReportExecutionRepository;
import dev.kreaker.hile.application.port.out.ReportExportRepository;
import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.domain.export.ReportExport;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportExecution;
import dev.kreaker.hile.domain.report.ReportParameter;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExportJobApplicationService implements ExportJobUseCase {

  private final ReportDefinitionRepository reportRepository;
  private final ReportParameterRepositoryPort parameterRepository;
  private final ReportExecutionRepository executionRepository;
  private final ReportExportRepository exportRepository;
  private final DataSourceAccessPort dataSourceAccessPort;
  private final String storageBasePath;
  private final int expiryHours;

  public ExportJobApplicationService(
      ReportDefinitionRepository reportRepository,
      ReportParameterRepositoryPort parameterRepository,
      ReportExecutionRepository executionRepository,
      ReportExportRepository exportRepository,
      DataSourceAccessPort dataSourceAccessPort,
      String storageBasePath,
      int expiryHours) {
    this.reportRepository = reportRepository;
    this.parameterRepository = parameterRepository;
    this.executionRepository = executionRepository;
    this.exportRepository = exportRepository;
    this.dataSourceAccessPort = dataSourceAccessPort;
    this.storageBasePath = storageBasePath;
    this.expiryHours = expiryHours;
  }

  @Override
  public ExportJobCreationResult requestExport(RequestExportCommand command, String requestedBy) {
    ReportDefinition def =
        reportRepository
            .findById(command.reportId())
            .orElseThrow(
                () -> new IllegalArgumentException("Report not found: " + command.reportId()));

    if (!dataSourceAccessPort.isAuthorized(def.dataSourceId(), requestedBy)) {
      throw new AccessDeniedException(
          "User does not have access to the datasource for this report.");
    }

    if (def.status() != ReportStatus.PUBLISHED) {
      throw new IllegalStateException("Only PUBLISHED reports can be exported.");
    }
    if (def.currentVersionId() == null) {
      throw new IllegalStateException("Report has no current version.");
    }

    List<ReportParameter> paramDefs = parameterRepository.findByReportId(command.reportId());
    Map<String, String> paramValues =
        command.parameterValues() != null ? command.parameterValues() : Map.of();

    NamedParamBinder.validateRequired(paramDefs, paramValues);
    NamedParamBinder.BoundSql bound = NamedParamBinder.bind(def.sqlText(), paramDefs, paramValues);

    UUID executionId = UUID.randomUUID();
    UUID exportId = UUID.randomUUID();
    String correlationId = UUID.randomUUID().toString();
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime expiresAt = now.plusHours(expiryHours);

    String format = command.format().toUpperCase();
    String storagePath = storageBasePath + "/" + exportId + "." + format.toLowerCase();
    int pageSize = command.pageSize() > 0 ? command.pageSize() : 5000;

    ReportExecution execution =
        new ReportExecution(
            executionId,
            def.id(),
            def.currentVersionId(),
            requestedBy,
            now,
            "RUNNING",
            "ASYNC",
            null,
            null,
            null,
            null,
            correlationId);
    executionRepository.save(execution, paramValues);

    ReportExport export =
        new ReportExport(exportId, executionId, format, storagePath, "PENDING", expiresAt);
    exportRepository.save(export);

    AsyncExportTask task =
        new AsyncExportTask(
            exportId,
            executionId,
            def.id(),
            def.currentVersionId(),
            def.dataSourceId(),
            bound.sql(),
            bound.values(),
            pageSize,
            format,
            storagePath);

    ExportJobView view = new ExportJobView(exportId, executionId, format, "PENDING", expiresAt);
    return new ExportJobCreationResult(view, task);
  }

  @Override
  public ExportJobView getExport(UUID exportId) {
    ReportExport export =
        exportRepository
            .findById(exportId)
            .orElseThrow(() -> new IllegalArgumentException("Export not found: " + exportId));
    return toView(export);
  }

  @Override
  public String getExportFilePath(UUID exportId) {
    ReportExport export =
        exportRepository
            .findById(exportId)
            .orElseThrow(() -> new IllegalArgumentException("Export not found: " + exportId));
    if (!"COMPLETED".equals(export.status())) {
      throw new IllegalStateException("Export not completed. Status: " + export.status());
    }
    return export.storagePath();
  }

  @Override
  public PageResult<ExportJobView> listExports(String requestedBy, int page, int size) {
    PageResult<ReportExport> result = exportRepository.findByRequestedBy(requestedBy, page, size);
    List<ExportJobView> content = result.content().stream().map(this::toView).toList();
    return new PageResult<>(content, page, size, result.total());
  }

  private ExportJobView toView(ReportExport e) {
    return new ExportJobView(e.id(), e.executionId(), e.format(), e.status(), e.expiresAt());
  }
}

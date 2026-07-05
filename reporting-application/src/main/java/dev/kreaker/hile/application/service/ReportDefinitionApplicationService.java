package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.CatalogReportView;
import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.ReportColumnView;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import dev.kreaker.hile.application.dto.ReportParameterView;
import dev.kreaker.hile.application.dto.UpdateReportCommand;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.out.QueryValidatorPort;
import dev.kreaker.hile.application.port.out.ReportColumnRepositoryPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.domain.report.ReportColumn;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportParameter;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReportDefinitionApplicationService implements CreateReportDefinitionUseCase {

  private final ReportDefinitionRepository repository;
  private final QueryValidatorPort queryValidator;
  private final DataSourceUseCase dataSourceUseCase;
  private final ReportColumnRepositoryPort columnRepository;
  private final ReportParameterRepositoryPort parameterRepository;
  private final int defaultMaxRows;
  private final int defaultTimeoutSeconds;

  public ReportDefinitionApplicationService(
      ReportDefinitionRepository repository,
      QueryValidatorPort queryValidator,
      DataSourceUseCase dataSourceUseCase,
      ReportColumnRepositoryPort columnRepository,
      ReportParameterRepositoryPort parameterRepository,
      int defaultMaxRows,
      int defaultTimeoutSeconds) {
    this.repository = repository;
    this.queryValidator = queryValidator;
    this.dataSourceUseCase = dataSourceUseCase;
    this.columnRepository = columnRepository;
    this.parameterRepository = parameterRepository;
    this.defaultMaxRows = defaultMaxRows;
    this.defaultTimeoutSeconds = defaultTimeoutSeconds;
  }

  @Override
  public ReportDefinitionView createDraft(CreateReportDefinitionCommand command) {
    var validation = queryValidator.validateReadOnly(command.sqlText());
    if (!validation.valid()) {
      throw new IllegalArgumentException(String.join(", ", validation.messages()));
    }
    ReportDefinition draft =
        new ReportDefinition(
            UUID.randomUUID(),
            command.name(),
            command.description(),
            command.categoryId(),
            command.dataSourceId(),
            command.ownerTeam(),
            ReportStatus.DRAFT,
            command.sqlText(),
            "PENDING",
            null,
            command.createdBy(),
            OffsetDateTime.now());
    return toView(repository.save(draft, defaultMaxRows, defaultTimeoutSeconds));
  }

  @Override
  public ReportDefinitionView updateDraft(UpdateReportCommand command) {
    ReportDefinition current =
        repository
            .findById(command.id())
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + command.id()));
    if (current.status() != ReportStatus.DRAFT) {
      throw new IllegalStateException("Only DRAFT reports can be updated.");
    }
    if (command.sqlText() != null) {
      var validation = queryValidator.validateReadOnly(command.sqlText());
      if (!validation.valid()) {
        throw new IllegalArgumentException(String.join(", ", validation.messages()));
      }
    }
    return toView(repository.updateDraft(command));
  }

  @Override
  public Optional<ReportDefinitionView> findById(UUID id) {
    return repository.findById(id).map(this::toView);
  }

  @Override
  public List<ReportDefinitionView> findAll() {
    return repository.findAll().stream().map(this::toView).toList();
  }

  @Override
  public PreviewResult runPreview(UUID reportId) {
    ReportDefinition def = repository.findById(reportId).orElseThrow(() -> notFound(reportId));
    if (def.sqlText() == null) {
      throw new IllegalStateException("Report has no SQL in its current version.");
    }
    try {
      PreviewResult result =
          dataSourceUseCase.executePreview(def.dataSourceId(), def.sqlText(), defaultMaxRows);
      repository.updatePreviewStatus(reportId, "VALID");
      return result;
    } catch (Exception e) {
      repository.updatePreviewStatus(reportId, "INVALID");
      throw e;
    }
  }

  @Override
  public ReportDefinitionView publish(UUID reportId) {
    ReportDefinition def = repository.findById(reportId).orElseThrow(() -> notFound(reportId));
    if (def.status() != ReportStatus.DRAFT) {
      throw new IllegalStateException("Only DRAFT reports can be published.");
    }
    if (!"VALID".equals(def.previewStatus())) {
      throw new IllegalStateException(
          "Report must have a successful preview before publishing. Run POST /api/v1/reports/"
              + reportId
              + "/preview first.");
    }
    return toView(repository.updateStatus(reportId, ReportStatus.PUBLISHED));
  }

  @Override
  public ReportDefinitionView unpublish(UUID reportId) {
    ReportDefinition def = repository.findById(reportId).orElseThrow(() -> notFound(reportId));
    if (def.status() != ReportStatus.PUBLISHED) {
      throw new IllegalStateException("Only PUBLISHED reports can be unpublished.");
    }
    return toView(repository.updateStatus(reportId, ReportStatus.DRAFT));
  }

  @Override
  public List<ReportColumnView> upsertColumns(UUID reportId, List<ReportColumn> columns) {
    repository.findById(reportId).orElseThrow(() -> notFound(reportId));
    return columnRepository.upsert(reportId, columns).stream().map(this::toColumnView).toList();
  }

  @Override
  public List<ReportColumnView> getColumns(UUID reportId) {
    return columnRepository.findByReportId(reportId).stream().map(this::toColumnView).toList();
  }

  @Override
  public List<ReportParameterView> upsertParameters(UUID reportId, List<ReportParameter> params) {
    repository.findById(reportId).orElseThrow(() -> notFound(reportId));
    return parameterRepository.upsert(reportId, params).stream()
        .map(this::toParameterView)
        .toList();
  }

  @Override
  public List<ReportParameterView> getParameters(UUID reportId) {
    return parameterRepository.findByReportId(reportId).stream()
        .map(this::toParameterView)
        .toList();
  }

  private ReportDefinitionView toView(ReportDefinition r) {
    return new ReportDefinitionView(
        r.id(),
        r.name(),
        r.description(),
        r.categoryId(),
        r.dataSourceId(),
        r.ownerTeam(),
        r.status(),
        r.sqlText(),
        r.previewStatus(),
        r.createdBy(),
        r.createdAt());
  }

  private ReportColumnView toColumnView(ReportColumn c) {
    return new ReportColumnView(
        c.id(),
        c.sourceName(),
        c.label(),
        c.dataType(),
        c.displayType(),
        c.displayFormat(),
        c.ordinal(),
        c.visible(),
        c.sortable(),
        c.filterableCandidate());
  }

  private ReportParameterView toParameterView(ReportParameter p) {
    return new ReportParameterView(
        p.id(),
        p.name(),
        p.label(),
        p.parameterType(),
        p.operatorType(),
        p.required(),
        p.defaultValue(),
        p.allowsMultiple(),
        p.sourceColumn(),
        p.validationRule());
  }

  @Override
  public List<CatalogReportView> getCatalog(String nameFilter) {
    return repository.findByStatus(ReportStatus.PUBLISHED).stream()
        .filter(
            r ->
                nameFilter == null
                    || nameFilter.isBlank()
                    || r.name().toLowerCase().contains(nameFilter.toLowerCase()))
        .map(this::toCatalogView)
        .toList();
  }

  private CatalogReportView toCatalogView(ReportDefinition r) {
    return new CatalogReportView(
        r.id(),
        r.name(),
        r.description(),
        r.dataSourceId(),
        r.ownerTeam(),
        r.createdBy(),
        r.createdAt());
  }

  private IllegalArgumentException notFound(UUID id) {
    return new IllegalArgumentException("Report not found: " + id);
  }
}

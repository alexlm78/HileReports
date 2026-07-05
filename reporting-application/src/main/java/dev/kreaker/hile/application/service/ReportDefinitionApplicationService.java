package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import dev.kreaker.hile.application.port.out.QueryValidatorPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReportDefinitionApplicationService implements CreateReportDefinitionUseCase {

  private final ReportDefinitionRepository repository;
  private final QueryValidatorPort queryValidator;
  private final int defaultMaxRows;
  private final int defaultTimeoutSeconds;

  public ReportDefinitionApplicationService(
      ReportDefinitionRepository repository,
      QueryValidatorPort queryValidator,
      int defaultMaxRows,
      int defaultTimeoutSeconds) {
    this.repository = repository;
    this.queryValidator = queryValidator;
    this.defaultMaxRows = defaultMaxRows;
    this.defaultTimeoutSeconds = defaultTimeoutSeconds;
  }

  @Override
  public ReportDefinitionView createDraft(CreateReportDefinitionCommand command) {
    ValidationResult validation = queryValidator.validateReadOnly(command.sqlText());
    if (!validation.valid()) {
      throw new IllegalArgumentException(String.join(", ", validation.messages()));
    }

    ReportDefinition draft =
        new ReportDefinition(
            UUID.randomUUID(),
            command.name(),
            command.description(),
            command.dataSourceId(),
            command.ownerTeam(),
            ReportStatus.DRAFT,
            command.sqlText(),
            command.createdBy(),
            OffsetDateTime.now());

    ReportDefinition saved = repository.save(draft, defaultMaxRows, defaultTimeoutSeconds);
    return toView(saved);
  }

  @Override
  public Optional<ReportDefinitionView> findById(UUID id) {
    return repository.findById(id).map(this::toView);
  }

  @Override
  public List<ReportDefinitionView> findAll() {
    return repository.findAll().stream().map(this::toView).toList();
  }

  private ReportDefinitionView toView(ReportDefinition r) {
    return new ReportDefinitionView(
        r.id(),
        r.name(),
        r.description(),
        r.dataSourceId(),
        r.ownerTeam(),
        r.status(),
        r.sqlText(),
        r.createdBy(),
        r.createdAt());
  }
}

package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import dev.kreaker.hile.application.port.out.QueryValidatorPort;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.util.UUID;

public class ReportDefinitionApplicationService implements CreateReportDefinitionUseCase {

  private final ReportDefinitionRepository reportDefinitionRepository;
  private final QueryValidatorPort queryValidatorPort;

  public ReportDefinitionApplicationService(
      ReportDefinitionRepository reportDefinitionRepository,
      QueryValidatorPort queryValidatorPort) {
    this.reportDefinitionRepository = reportDefinitionRepository;
    this.queryValidatorPort = queryValidatorPort;
  }

  @Override
  public ReportDefinition createDraft(CreateReportDefinitionCommand command) {
    ValidationResult validationResult = queryValidatorPort.validateReadOnly(command.sqlText());
    if (!validationResult.valid()) {
      throw new IllegalArgumentException(String.join(", ", validationResult.messages()));
    }

    ReportDefinition reportDefinition =
        new ReportDefinition(
            UUID.randomUUID(),
            command.name(),
            command.description(),
            command.dataSourceType(),
            ReportStatus.DRAFT);

    return reportDefinitionRepository.save(reportDefinition);
  }
}

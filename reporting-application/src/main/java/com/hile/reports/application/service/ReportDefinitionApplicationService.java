package com.hile.reports.application.service;

import com.hile.reports.application.dto.CreateReportDefinitionCommand;
import com.hile.reports.application.dto.ValidationResult;
import com.hile.reports.application.port.in.CreateReportDefinitionUseCase;
import com.hile.reports.application.port.out.QueryValidatorPort;
import com.hile.reports.application.port.out.ReportDefinitionRepository;
import com.hile.reports.domain.report.ReportDefinition;
import com.hile.reports.domain.report.ReportStatus;
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

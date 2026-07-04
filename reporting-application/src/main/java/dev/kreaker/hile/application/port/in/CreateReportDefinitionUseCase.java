package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.domain.report.ReportDefinition;

public interface CreateReportDefinitionUseCase {
  ReportDefinition createDraft(CreateReportDefinitionCommand command);
}

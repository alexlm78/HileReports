package com.hile.reports.application.port.in;

import com.hile.reports.application.dto.CreateReportDefinitionCommand;
import com.hile.reports.domain.report.ReportDefinition;

public interface CreateReportDefinitionUseCase {
  ReportDefinition createDraft(CreateReportDefinitionCommand command);
}

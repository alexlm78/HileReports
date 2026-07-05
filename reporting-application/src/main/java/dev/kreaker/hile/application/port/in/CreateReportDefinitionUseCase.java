package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreateReportDefinitionUseCase {

  ReportDefinitionView createDraft(CreateReportDefinitionCommand command);

  Optional<ReportDefinitionView> findById(UUID id);

  List<ReportDefinitionView> findAll();
}

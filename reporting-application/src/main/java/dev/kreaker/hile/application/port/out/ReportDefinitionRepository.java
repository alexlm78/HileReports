package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.UpdateReportCommand;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportDefinitionRepository {

  ReportDefinition save(ReportDefinition reportDefinition, int maxRows, int timeoutSeconds);

  ReportDefinition updateDraft(UpdateReportCommand command);

  PageResult<ReportDefinition> findAllPaged(
      int page, int size, String name, String status, UUID categoryId);

  Optional<ReportDefinition> findById(UUID id);

  List<ReportDefinition> findAll();

  List<ReportDefinition> findByStatus(ReportStatus status);

  ReportDefinition updateStatus(UUID id, ReportStatus newStatus);

  ReportDefinition updatePreviewStatus(UUID id, String previewStatus);

  boolean hasExecutions(UUID id);

  void deleteDraft(UUID id);
}

package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CatalogReportView;
import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.ReportColumnView;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import dev.kreaker.hile.application.dto.ReportParameterView;
import dev.kreaker.hile.application.dto.UpdateReportCommand;
import dev.kreaker.hile.domain.report.ReportColumn;
import dev.kreaker.hile.domain.report.ReportParameter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CreateReportDefinitionUseCase {

  ReportDefinitionView createDraft(CreateReportDefinitionCommand command);

  ReportDefinitionView updateDraft(UpdateReportCommand command);

  Optional<ReportDefinitionView> findById(UUID id);

  List<ReportDefinitionView> findAll();

  PreviewResult runPreview(UUID reportId);

  ReportDefinitionView publish(UUID reportId);

  ReportDefinitionView unpublish(UUID reportId);

  List<ReportColumnView> upsertColumns(UUID reportId, List<ReportColumn> columns);

  List<ReportColumnView> getColumns(UUID reportId);

  List<ReportParameterView> upsertParameters(UUID reportId, List<ReportParameter> parameters);

  List<ReportParameterView> getParameters(UUID reportId);

  List<CatalogReportView> getCatalog(String nameFilter);

  void deleteDraft(UUID id);
}

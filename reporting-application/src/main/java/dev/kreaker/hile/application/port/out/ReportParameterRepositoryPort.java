package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.domain.report.ReportParameter;
import java.util.List;
import java.util.UUID;

public interface ReportParameterRepositoryPort {

  List<ReportParameter> upsert(UUID reportId, List<ReportParameter> parameters);

  List<ReportParameter> findByReportId(UUID reportId);
}

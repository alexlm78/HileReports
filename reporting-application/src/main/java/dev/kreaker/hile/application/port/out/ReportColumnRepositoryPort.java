package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.domain.report.ReportColumn;
import java.util.List;
import java.util.UUID;

public interface ReportColumnRepositoryPort {

  List<ReportColumn> upsert(UUID reportId, List<ReportColumn> columns);

  List<ReportColumn> findByReportId(UUID reportId);
}

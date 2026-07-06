package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.domain.report.ReportExecution;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ReportExecutionRepository {

  ReportExecution save(ReportExecution execution, Map<String, String> paramValues);

  Optional<ReportExecution> findById(UUID id);

  void updateStatus(UUID executionId, String status, Long rowCount, Long durationMs);

  PageResult<ReportExecution> findByReportId(UUID reportDefinitionId, int page, int size);

  PageResult<ReportExecution> findByRequestedBy(String requestedBy, int page, int size);
}

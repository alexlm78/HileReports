package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.domain.export.ReportExport;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportExportRepository {

  ReportExport save(ReportExport export);

  Optional<ReportExport> findById(UUID id);

  void updateStatus(UUID id, String status);

  void updateStatusAndPath(UUID id, String status, String storagePath);

  List<ReportExport> findExpiredBefore(OffsetDateTime cutoff);

  void deleteById(UUID id);

  PageResult<ReportExport> findByRequestedBy(String requestedBy, int page, int size);
}

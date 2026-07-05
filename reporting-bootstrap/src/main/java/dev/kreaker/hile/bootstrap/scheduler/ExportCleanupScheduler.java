package dev.kreaker.hile.bootstrap.scheduler;

import dev.kreaker.hile.application.port.out.ReportExportRepository;
import dev.kreaker.hile.domain.export.ReportExport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExportCleanupScheduler {

  private static final Logger log = LoggerFactory.getLogger(ExportCleanupScheduler.class);

  private final ReportExportRepository exportRepository;

  public ExportCleanupScheduler(ReportExportRepository exportRepository) {
    this.exportRepository = exportRepository;
  }

  @Scheduled(
      fixedDelayString = "${hile.reports.export.cleanup-interval-ms:3600000}",
      initialDelayString = "${hile.reports.export.cleanup-initial-delay-ms:60000}")
  public void cleanupExpiredExports() {
    List<ReportExport> expired = exportRepository.findExpiredBefore(OffsetDateTime.now());
    for (ReportExport export : expired) {
      if (export.storagePath() != null && !export.storagePath().isBlank()) {
        try {
          Files.deleteIfExists(Path.of(export.storagePath()));
        } catch (IOException e) {
          log.warn("Failed to delete export file {}: {}", export.storagePath(), e.getMessage());
        }
      }
      exportRepository.deleteById(export.id());
    }
    if (!expired.isEmpty()) {
      log.info("Cleaned up {} expired exports.", expired.size());
    }
  }
}

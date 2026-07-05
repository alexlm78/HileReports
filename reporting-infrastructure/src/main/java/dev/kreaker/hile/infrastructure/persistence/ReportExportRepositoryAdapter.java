package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.ReportExportRepository;
import dev.kreaker.hile.domain.export.ReportExport;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportExportEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportExportJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ReportExportRepositoryAdapter implements ReportExportRepository {

  private final ReportExportJpaRepository repo;

  public ReportExportRepositoryAdapter(ReportExportJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public ReportExport save(ReportExport export) {
    return toDomain(
        repo.save(
            new ReportExportEntity(
                export.id(),
                export.executionId(),
                export.format(),
                export.storagePath(),
                export.status(),
                export.expiresAt())));
  }

  @Override
  public Optional<ReportExport> findById(UUID id) {
    return repo.findById(id).map(this::toDomain);
  }

  @Override
  public void updateStatus(UUID id, String status) {
    repo.updateStatus(id, status);
  }

  @Override
  public void updateStatusAndPath(UUID id, String status, String storagePath) {
    repo.updateStatusAndPath(id, status, storagePath);
  }

  @Override
  public List<ReportExport> findExpiredBefore(OffsetDateTime cutoff) {
    return repo.findByExpiresAtBefore(cutoff).stream().map(this::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    repo.deleteById(id);
  }

  private ReportExport toDomain(ReportExportEntity e) {
    return new ReportExport(
        e.getId(),
        e.getExecutionId(),
        e.getFormat(),
        e.getStoragePath(),
        e.getStatus(),
        e.getExpiresAt());
  }
}

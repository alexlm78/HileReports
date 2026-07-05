package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.ReportColumnRepositoryPort;
import dev.kreaker.hile.domain.report.ReportColumn;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportColumnEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportColumnJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportDefinitionJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReportColumnRepositoryAdapter implements ReportColumnRepositoryPort {

  private final ReportColumnJpaRepository columnRepo;
  private final ReportDefinitionJpaRepository defRepo;

  public ReportColumnRepositoryAdapter(
      ReportColumnJpaRepository columnRepo, ReportDefinitionJpaRepository defRepo) {
    this.columnRepo = columnRepo;
    this.defRepo = defRepo;
  }

  @Override
  @Transactional
  public List<ReportColumn> upsert(UUID reportId, List<ReportColumn> columns) {
    UUID versionId = currentVersionId(reportId);
    columnRepo.deleteByReportVersionId(versionId);
    columnRepo.flush();
    List<ReportColumnEntity> entities = columns.stream().map(c -> toEntity(c, versionId)).toList();
    columnRepo.saveAll(entities);
    return entities.stream().map(this::toDomain).toList();
  }

  @Override
  public List<ReportColumn> findByReportId(UUID reportId) {
    UUID versionId = currentVersionId(reportId);
    return columnRepo.findByReportVersionIdOrderByOrdinalAsc(versionId).stream()
        .map(this::toDomain)
        .toList();
  }

  private UUID currentVersionId(UUID reportId) {
    ReportDefinitionEntity def =
        defRepo
            .findById(reportId)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
    if (def.getCurrentVersionId() == null) {
      throw new IllegalStateException("Report has no current version: " + reportId);
    }
    return def.getCurrentVersionId();
  }

  private ReportColumnEntity toEntity(ReportColumn c, UUID versionId) {
    return new ReportColumnEntity(
        c.id() != null ? c.id() : UUID.randomUUID(),
        versionId,
        c.sourceName(),
        c.label(),
        c.dataType(),
        c.displayType(),
        c.displayFormat(),
        c.ordinal(),
        c.visible(),
        c.sortable(),
        c.filterableCandidate());
  }

  private ReportColumn toDomain(ReportColumnEntity e) {
    return new ReportColumn(
        e.getId(),
        e.getReportVersionId(),
        e.getSourceName(),
        e.getLabel(),
        e.getDataType(),
        e.getDisplayType(),
        e.getDisplayFormat(),
        e.getOrdinal(),
        e.isVisible(),
        e.isSortable(),
        e.isFilterableCandidate());
  }
}

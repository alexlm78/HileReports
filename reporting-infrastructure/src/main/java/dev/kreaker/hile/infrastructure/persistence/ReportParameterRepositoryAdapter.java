package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.ReportParameterRepositoryPort;
import dev.kreaker.hile.domain.report.ReportParameter;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportParameterEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportDefinitionJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportParameterJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReportParameterRepositoryAdapter implements ReportParameterRepositoryPort {

  private final ReportParameterJpaRepository paramRepo;
  private final ReportDefinitionJpaRepository defRepo;

  public ReportParameterRepositoryAdapter(
      ReportParameterJpaRepository paramRepo, ReportDefinitionJpaRepository defRepo) {
    this.paramRepo = paramRepo;
    this.defRepo = defRepo;
  }

  @Override
  @Transactional
  public List<ReportParameter> upsert(UUID reportId, List<ReportParameter> parameters) {
    UUID versionId = currentVersionId(reportId);
    paramRepo.deleteByReportVersionId(versionId);
    paramRepo.flush();
    List<ReportParameterEntity> entities =
        parameters.stream().map(p -> toEntity(p, versionId)).toList();
    paramRepo.saveAll(entities);
    return entities.stream().map(this::toDomain).toList();
  }

  @Override
  public List<ReportParameter> findByReportId(UUID reportId) {
    UUID versionId = currentVersionId(reportId);
    return paramRepo.findByReportVersionId(versionId).stream().map(this::toDomain).toList();
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

  private ReportParameterEntity toEntity(ReportParameter p, UUID versionId) {
    return new ReportParameterEntity(
        p.id() != null ? p.id() : UUID.randomUUID(),
        versionId,
        p.name(),
        p.label(),
        p.parameterType(),
        p.operatorType(),
        p.required(),
        p.defaultValue(),
        p.allowsMultiple(),
        p.sourceColumn(),
        p.validationRule());
  }

  private ReportParameter toDomain(ReportParameterEntity e) {
    return new ReportParameter(
        e.getId(),
        e.getReportVersionId(),
        e.getName(),
        e.getLabel(),
        e.getParameterType(),
        e.getOperatorType(),
        e.isRequired(),
        e.getDefaultValue(),
        e.isAllowsMultiple(),
        e.getSourceColumn(),
        e.getValidationRule());
  }
}

package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportStatus;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportVersionEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportDefinitionJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportVersionJpaRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReportDefinitionRepositoryAdapter implements ReportDefinitionRepository {

  private final ReportDefinitionJpaRepository defRepo;
  private final ReportVersionJpaRepository versionRepo;

  public ReportDefinitionRepositoryAdapter(
      ReportDefinitionJpaRepository defRepo, ReportVersionJpaRepository versionRepo) {
    this.defRepo = defRepo;
    this.versionRepo = versionRepo;
  }

  @Override
  @Transactional
  public ReportDefinition save(ReportDefinition def, int maxRows, int timeoutSeconds) {
    ReportDefinitionEntity entity =
        new ReportDefinitionEntity(
            def.id(),
            def.name(),
            def.description(),
            def.dataSourceId(),
            def.ownerTeam(),
            def.status().name(),
            def.createdBy(),
            def.createdAt());
    defRepo.save(entity);
    defRepo.flush();

    ReportVersionEntity version =
        new ReportVersionEntity(
            UUID.randomUUID(),
            def.id(),
            1,
            def.sqlText(),
            sha256(def.sqlText()),
            "VALIDATED",
            "PENDING",
            maxRows,
            timeoutSeconds,
            "SYNC",
            def.createdBy(),
            OffsetDateTime.now());
    versionRepo.save(version);
    versionRepo.flush();

    entity.setCurrentVersionId(version.getId());
    defRepo.save(entity);

    return toDomain(entity, def.sqlText());
  }

  @Override
  public Optional<ReportDefinition> findById(UUID id) {
    return defRepo
        .findById(id)
        .map(
            e -> {
              String sql = latestSql(e.getId());
              return toDomain(e, sql);
            });
  }

  @Override
  public List<ReportDefinition> findAll() {
    return defRepo.findAll().stream().map(e -> toDomain(e, null)).toList();
  }

  private String latestSql(UUID defId) {
    List<ReportVersionEntity> versions =
        versionRepo.findByReportDefinitionIdOrderByVersionNumberAsc(defId);
    if (versions.isEmpty()) return null;
    return versions.get(versions.size() - 1).getSqlText();
  }

  private ReportDefinition toDomain(ReportDefinitionEntity e, String sqlText) {
    return new ReportDefinition(
        e.getId(),
        e.getName(),
        e.getDescription(),
        e.getDataSourceId(),
        e.getOwnerTeam(),
        ReportStatus.valueOf(e.getStatus()),
        sqlText,
        e.getCreatedBy(),
        e.getCreatedAt());
  }

  private static String sha256(String text) {
    try {
      byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}

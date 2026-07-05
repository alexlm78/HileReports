package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.dto.UpdateReportCommand;
import dev.kreaker.hile.application.port.out.ReportDefinitionRepository;
import dev.kreaker.hile.domain.report.ReportDefinition;
import dev.kreaker.hile.domain.report.ReportStatus;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.ReportVersionEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportColumnJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportDefinitionJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportExecutionJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportParameterJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.ReportTagJpaRepository;
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
  private final ReportColumnJpaRepository columnRepo;
  private final ReportParameterJpaRepository parameterRepo;
  private final ReportTagJpaRepository tagRepo;
  private final ReportExecutionJpaRepository executionRepo;

  public ReportDefinitionRepositoryAdapter(
      ReportDefinitionJpaRepository defRepo,
      ReportVersionJpaRepository versionRepo,
      ReportColumnJpaRepository columnRepo,
      ReportParameterJpaRepository parameterRepo,
      ReportTagJpaRepository tagRepo,
      ReportExecutionJpaRepository executionRepo) {
    this.defRepo = defRepo;
    this.versionRepo = versionRepo;
    this.columnRepo = columnRepo;
    this.parameterRepo = parameterRepo;
    this.tagRepo = tagRepo;
    this.executionRepo = executionRepo;
  }

  @Override
  @Transactional
  public ReportDefinition save(ReportDefinition def, int maxRows, int timeoutSeconds) {
    ReportDefinitionEntity entity =
        new ReportDefinitionEntity(
            def.id(),
            def.name(),
            def.description(),
            def.categoryId(),
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

    return toDomain(entity, def.sqlText(), "PENDING");
  }

  @Override
  public Optional<ReportDefinition> findById(UUID id) {
    return defRepo
        .findById(id)
        .map(
            e -> {
              VersionInfo info = latestVersionInfo(e.getId());
              return toDomain(e, info.sqlText(), info.previewStatus());
            });
  }

  @Override
  public List<ReportDefinition> findAll() {
    return defRepo.findAll().stream().map(e -> toDomain(e, null, null)).toList();
  }

  @Override
  public List<ReportDefinition> findByStatus(ReportStatus status) {
    return defRepo.findByStatusOrderByCreatedAtDesc(status.name()).stream()
        .map(e -> toDomain(e, null, null))
        .toList();
  }

  @Override
  @Transactional
  public ReportDefinition updateDraft(UpdateReportCommand command) {
    ReportDefinitionEntity entity =
        defRepo
            .findById(command.id())
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + command.id()));
    if (command.name() != null) entity.setName(command.name());
    if (command.description() != null) entity.setDescription(command.description());
    if (command.categoryId() != null) entity.setCategoryId(command.categoryId());
    if (command.dataSourceId() != null) entity.setDataSourceId(command.dataSourceId());
    if (command.ownerTeam() != null) entity.setOwnerTeam(command.ownerTeam());
    defRepo.save(entity);

    VersionInfo info = latestVersionInfo(command.id());
    if (command.sqlText() != null && entity.getCurrentVersionId() != null) {
      versionRepo
          .findById(entity.getCurrentVersionId())
          .ifPresent(
              v -> {
                v.setSqlText(command.sqlText());
                v.setPreviewStatus("PENDING");
                versionRepo.save(v);
              });
      return toDomain(entity, command.sqlText(), "PENDING");
    }
    return toDomain(entity, info.sqlText(), info.previewStatus());
  }

  @Override
  @Transactional
  public ReportDefinition updateStatus(UUID id, ReportStatus newStatus) {
    ReportDefinitionEntity entity =
        defRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    entity.setStatus(newStatus.name());
    defRepo.save(entity);
    VersionInfo info = latestVersionInfo(id);
    return toDomain(entity, info.sqlText(), info.previewStatus());
  }

  @Override
  @Transactional
  public ReportDefinition updatePreviewStatus(UUID id, String previewStatus) {
    ReportDefinitionEntity entity =
        defRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    UUID versionId = entity.getCurrentVersionId();
    if (versionId != null) {
      versionRepo
          .findById(versionId)
          .ifPresent(
              v -> {
                v.setPreviewStatus(previewStatus);
                versionRepo.save(v);
              });
    }
    VersionInfo info = latestVersionInfo(id);
    return toDomain(entity, info.sqlText(), info.previewStatus());
  }

  @Override
  public boolean hasExecutions(UUID id) {
    return executionRepo.existsByReportDefinitionId(id);
  }

  @Override
  @Transactional
  public void deleteDraft(UUID id) {
    ReportDefinitionEntity entity =
        defRepo
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Report not found: " + id));
    List<ReportVersionEntity> versions =
        versionRepo.findByReportDefinitionIdOrderByVersionNumberAsc(id);
    for (ReportVersionEntity v : versions) {
      columnRepo.deleteByReportVersionId(v.getId());
      parameterRepo.deleteByReportVersionId(v.getId());
    }
    tagRepo.deleteByIdReportDefinitionId(id);
    entity.setCurrentVersionId(null);
    defRepo.save(entity);
    defRepo.flush();
    versionRepo.deleteByReportDefinitionId(id);
    defRepo.delete(entity);
  }

  private VersionInfo latestVersionInfo(UUID defId) {
    List<ReportVersionEntity> versions =
        versionRepo.findByReportDefinitionIdOrderByVersionNumberAsc(defId);
    if (versions.isEmpty()) return new VersionInfo(null, null);
    ReportVersionEntity latest = versions.get(versions.size() - 1);
    return new VersionInfo(latest.getSqlText(), latest.getPreviewStatus());
  }

  private ReportDefinition toDomain(
      ReportDefinitionEntity e, String sqlText, String previewStatus) {
    return new ReportDefinition(
        e.getId(),
        e.getName(),
        e.getDescription(),
        e.getCategoryId(),
        e.getDataSourceId(),
        e.getOwnerTeam(),
        ReportStatus.valueOf(e.getStatus()),
        sqlText,
        previewStatus,
        e.getCurrentVersionId(),
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

  private record VersionInfo(String sqlText, String previewStatus) {}
}

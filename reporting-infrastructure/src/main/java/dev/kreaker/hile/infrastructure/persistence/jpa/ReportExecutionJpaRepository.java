package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportExecutionEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReportExecutionJpaRepository extends JpaRepository<ReportExecutionEntity, UUID> {

  boolean existsByReportDefinitionId(UUID reportDefinitionId);

  Page<ReportExecutionEntity> findByReportDefinitionIdOrderByRequestedAtDesc(
      UUID reportDefinitionId, Pageable pageable);

  Page<ReportExecutionEntity> findByRequestedByOrderByRequestedAtDesc(
      String requestedBy, Pageable pageable);

  @Modifying
  @Transactional
  @Query(
      "UPDATE ReportExecutionEntity e SET e.status = :status, e.rowCount = :rowCount,"
          + " e.durationMs = :durationMs WHERE e.id = :id")
  void updateStatus(
      @Param("id") UUID id,
      @Param("status") String status,
      @Param("rowCount") Long rowCount,
      @Param("durationMs") Long durationMs);
}

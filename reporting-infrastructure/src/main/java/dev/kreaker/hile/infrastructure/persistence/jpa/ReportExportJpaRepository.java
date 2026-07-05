package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportExportEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReportExportJpaRepository extends JpaRepository<ReportExportEntity, UUID> {

  List<ReportExportEntity> findByExpiresAtBefore(OffsetDateTime cutoff);

  @Modifying
  @Transactional
  @Query(
      "UPDATE ReportExportEntity e SET e.status = :status, e.storagePath = :storagePath"
          + " WHERE e.id = :id")
  void updateStatusAndPath(
      @Param("id") UUID id,
      @Param("status") String status,
      @Param("storagePath") String storagePath);

  @Modifying
  @Transactional
  @Query("UPDATE ReportExportEntity e SET e.status = :status WHERE e.id = :id")
  void updateStatus(@Param("id") UUID id, @Param("status") String status);
}

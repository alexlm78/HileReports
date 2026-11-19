package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportColumnEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportColumnJpaRepository extends JpaRepository<ReportColumnEntity, UUID> {

   List<ReportColumnEntity> findByReportVersionIdOrderByOrdinalAsc(UUID reportVersionId);

   void deleteByReportVersionId(UUID reportVersionId);
}

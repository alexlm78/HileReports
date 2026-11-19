package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportVersionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportVersionJpaRepository extends JpaRepository<ReportVersionEntity, UUID> {

   List<ReportVersionEntity> findByReportDefinitionIdOrderByVersionNumberAsc(
         UUID reportDefinitionId);

   void deleteByReportDefinitionId(UUID reportDefinitionId);
}

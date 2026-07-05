package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportTagEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTagJpaRepository extends JpaRepository<ReportTagEntity, ReportTagEntity.Id> {

  void deleteByIdReportDefinitionId(UUID reportDefinitionId);
}

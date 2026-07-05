package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportVersionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportVersionJpaRepository extends JpaRepository<ReportVersionEntity, UUID> {

  List<ReportVersionEntity> findByReportDefinitionIdOrderByVersionNumberAsc(
      UUID reportDefinitionId);
}

package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportParameterEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportParameterJpaRepository extends JpaRepository<ReportParameterEntity, UUID> {

  List<ReportParameterEntity> findByReportVersionId(UUID reportVersionId);

  void deleteByReportVersionId(UUID reportVersionId);
}

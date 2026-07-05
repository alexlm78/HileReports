package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportDefinitionJpaRepository extends JpaRepository<ReportDefinitionEntity, UUID> {

  List<ReportDefinitionEntity> findByStatusOrderByCreatedAtDesc(String status);
}

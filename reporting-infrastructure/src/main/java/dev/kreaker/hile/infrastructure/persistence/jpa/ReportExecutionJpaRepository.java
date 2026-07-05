package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportExecutionEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExecutionJpaRepository extends JpaRepository<ReportExecutionEntity, UUID> {}

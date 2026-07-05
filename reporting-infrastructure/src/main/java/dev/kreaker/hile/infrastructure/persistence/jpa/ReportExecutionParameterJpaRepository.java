package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportExecutionParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExecutionParameterJpaRepository
    extends JpaRepository<ReportExecutionParameterEntity, ReportExecutionParameterEntity.Id> {}

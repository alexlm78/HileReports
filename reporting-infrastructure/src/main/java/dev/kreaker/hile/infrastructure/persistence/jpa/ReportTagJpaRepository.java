package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportTagEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportTagJpaRepository extends JpaRepository<ReportTagEntity, ReportTagEntity.Id> {

   void deleteByIdReportDefinitionId(UUID reportDefinitionId);
}

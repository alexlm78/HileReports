package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.TagEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagJpaRepository extends JpaRepository<TagEntity, UUID> {

  boolean existsByName(String name);

  boolean existsBySlug(String slug);

  List<TagEntity> findByIdIn(List<UUID> ids);

  @Query(
      "SELECT t FROM TagEntity t WHERE t.id IN "
          + "(SELECT rt.id.tagId FROM ReportTagEntity rt WHERE rt.id.reportDefinitionId = :reportId)")
  List<TagEntity> findByReportDefinitionId(@Param("reportId") UUID reportId);
}

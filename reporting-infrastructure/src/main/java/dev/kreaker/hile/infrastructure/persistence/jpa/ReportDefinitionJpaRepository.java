package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.ReportDefinitionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportDefinitionJpaRepository extends JpaRepository<ReportDefinitionEntity, UUID> {

  List<ReportDefinitionEntity> findByStatusOrderByCreatedAtDesc(String status);

  @Query(
      "SELECT r FROM ReportDefinitionEntity r WHERE "
          + "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
          + "(:status IS NULL OR r.status = :status) AND "
          + "(:categoryId IS NULL OR r.categoryId = :categoryId) "
          + "ORDER BY r.createdAt DESC")
  Page<ReportDefinitionEntity> findAllFiltered(
      @Param("name") String name,
      @Param("status") String status,
      @Param("categoryId") UUID categoryId,
      Pageable pageable);
}

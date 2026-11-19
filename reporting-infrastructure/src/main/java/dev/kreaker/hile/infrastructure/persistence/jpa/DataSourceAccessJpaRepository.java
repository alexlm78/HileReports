package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DataSourceAccessJpaRepository
      extends JpaRepository<DataSourceAccessEntity, DataSourceAccessId> {

   List<DataSourceAccessEntity> findByIdDatasourceId(UUID datasourceId);
}

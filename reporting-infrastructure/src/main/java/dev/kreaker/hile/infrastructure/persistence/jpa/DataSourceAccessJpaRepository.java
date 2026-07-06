package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSourceAccessJpaRepository
    extends JpaRepository<DataSourceAccessEntity, DataSourceAccessId> {

  List<DataSourceAccessEntity> findByIdDatasourceId(UUID datasourceId);
}

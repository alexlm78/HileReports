package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.DataSourceRepositoryPort;
import dev.kreaker.hile.domain.datasource.DataSource;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.DataSourceJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class DataSourceRepositoryAdapter implements DataSourceRepositoryPort {

  private final DataSourceJpaRepository jpaRepository;

  public DataSourceRepositoryAdapter(DataSourceJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public DataSource save(DataSource ds) {
    DataSourceEntity entity =
        new DataSourceEntity(
            ds.id(),
            ds.name(),
            ds.dbType(),
            ds.host(),
            ds.port(),
            ds.databaseOrService(),
            ds.username(),
            ds.secretRef(),
            ds.sslMode(),
            ds.status(),
            ds.createdBy(),
            ds.createdAt());
    return toDomain(jpaRepository.save(entity));
  }

  @Override
  public Optional<DataSource> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<DataSource> findAll() {
    return jpaRepository.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }

  private DataSource toDomain(DataSourceEntity e) {
    return new DataSource(
        e.getId(),
        e.getName(),
        e.getDbType(),
        e.getHost(),
        e.getPort(),
        e.getDatabaseOrService(),
        e.getUsername(),
        e.getSecretRef(),
        e.getSslMode(),
        e.getStatus(),
        e.getCreatedBy(),
        e.getCreatedAt());
  }
}

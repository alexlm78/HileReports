package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.UpdateDataSourceCommand;
import dev.kreaker.hile.application.port.out.DataSourceRepositoryPort;
import dev.kreaker.hile.domain.datasource.DataSource;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.DataSourceJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
  public PageResult<DataSource> findAllPaged(int page, int size) {
    Page<DataSourceEntity> result = jpaRepository.findAll(PageRequest.of(page, size));
    List<DataSource> content = result.getContent().stream().map(this::toDomain).toList();
    return new PageResult<>(content, page, size, result.getTotalElements());
  }

  @Override
  public void deleteById(UUID id) {
    jpaRepository.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return jpaRepository.existsById(id);
  }

  @Override
  public DataSource update(UpdateDataSourceCommand command) {
    DataSourceEntity entity =
        jpaRepository
            .findById(command.id())
            .orElseThrow(
                () -> new IllegalArgumentException("DataSource not found: " + command.id()));
    if (command.name() != null) entity.setName(command.name());
    if (command.host() != null) entity.setHost(command.host());
    if (command.port() != null) entity.setPort(command.port());
    if (command.databaseOrService() != null)
      entity.setDatabaseOrService(command.databaseOrService());
    if (command.username() != null) entity.setUsername(command.username());
    if (command.password() != null) entity.setSecretRef(command.password());
    if (command.sslMode() != null) entity.setSslMode(command.sslMode());
    return toDomain(jpaRepository.save(entity));
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

package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.CreateDataSourceCommand;
import dev.kreaker.hile.application.dto.DataSourceView;
import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.UpdateDataSourceCommand;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.exception.DataSourceNotFoundException;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.out.DataSourceRepositoryPort;
import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.application.port.out.PasswordEncryptionPort;
import dev.kreaker.hile.domain.datasource.DataSource;
import dev.kreaker.hile.domain.datasource.DataSourceStatus;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataSourceApplicationService implements DataSourceUseCase {

  private final DataSourceRepositoryPort repository;
  private final PasswordEncryptionPort encryption;
  private final Map<DataSourceType, DbConnectorPort> connectors;

  public DataSourceApplicationService(
      DataSourceRepositoryPort repository,
      PasswordEncryptionPort encryption,
      List<DbConnectorPort> connectorList) {
    this.repository = repository;
    this.encryption = encryption;
    this.connectors =
        connectorList.stream()
            .collect(
                Collectors.toMap(
                    c ->
                        List.of(DataSourceType.values()).stream()
                            .filter(c::supports)
                            .findFirst()
                            .orElseThrow(),
                    Function.identity()));
  }

  @Override
  public DataSourceView create(CreateDataSourceCommand command) {
    String secretRef = encryption.encrypt(command.rawPassword());
    DataSource ds =
        new DataSource(
            UUID.randomUUID(),
            command.name(),
            command.dbType(),
            command.host(),
            command.port(),
            command.databaseOrService(),
            command.username(),
            secretRef,
            command.sslMode(),
            DataSourceStatus.ACTIVE,
            command.createdBy(),
            OffsetDateTime.now());
    return toView(repository.save(ds));
  }

  @Override
  public DataSourceView findById(UUID id) {
    return repository
        .findById(id)
        .map(this::toView)
        .orElseThrow(() -> new DataSourceNotFoundException(id));
  }

  @Override
  public List<DataSourceView> findAll() {
    return repository.findAll().stream().map(this::toView).toList();
  }

  @Override
  public PageResult<DataSourceView> findAllPaged(int page, int size) {
    PageResult<DataSource> result = repository.findAllPaged(page, size);
    List<DataSourceView> content = result.content().stream().map(this::toView).toList();
    return new PageResult<>(content, page, size, result.total());
  }

  @Override
  public DataSourceView update(UpdateDataSourceCommand command) {
    if (!repository.existsById(command.id())) {
      throw new DataSourceNotFoundException(command.id());
    }
    String encryptedSecret =
        command.password() != null ? encryption.encrypt(command.password()) : null;
    UpdateDataSourceCommand cmd =
        new UpdateDataSourceCommand(
            command.id(),
            command.name(),
            command.host(),
            command.port(),
            command.databaseOrService(),
            command.username(),
            encryptedSecret,
            command.sslMode());
    return toView(repository.update(cmd));
  }

  @Override
  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new DataSourceNotFoundException(id);
    }
    repository.deleteById(id);
  }

  @Override
  public ValidationResult testConnection(UUID id) {
    DataSource ds = repository.findById(id).orElseThrow(() -> new DataSourceNotFoundException(id));
    String rawPassword = encryption.decrypt(ds.secretRef());
    String jdbcUrl = buildJdbcUrl(ds.dbType(), ds.host(), ds.port(), ds.databaseOrService());
    DbConnectorPort connector = connectors.get(ds.dbType());
    if (connector == null) {
      return ValidationResult.failure("No connector available for type: " + ds.dbType());
    }
    return connector.testConnection(jdbcUrl, ds.username(), rawPassword);
  }

  @Override
  public List<ColumnMetadata> discoverColumns(UUID dataSourceId, String sqlText) {
    DataSource ds =
        repository
            .findById(dataSourceId)
            .orElseThrow(() -> new DataSourceNotFoundException(dataSourceId));
    String rawPassword = encryption.decrypt(ds.secretRef());
    String jdbcUrl = buildJdbcUrl(ds.dbType(), ds.host(), ds.port(), ds.databaseOrService());
    DbConnectorPort connector = connectors.get(ds.dbType());
    if (connector == null) {
      throw new IllegalStateException("No connector available for type: " + ds.dbType());
    }
    return connector.discoverColumns(jdbcUrl, ds.username(), rawPassword, sqlText);
  }

  @Override
  public PreviewResult executePreview(UUID dataSourceId, String sqlText, int maxRows) {
    DataSource ds =
        repository
            .findById(dataSourceId)
            .orElseThrow(() -> new DataSourceNotFoundException(dataSourceId));
    String rawPassword = encryption.decrypt(ds.secretRef());
    String jdbcUrl = buildJdbcUrl(ds.dbType(), ds.host(), ds.port(), ds.databaseOrService());
    DbConnectorPort connector = connectors.get(ds.dbType());
    if (connector == null) {
      throw new IllegalStateException("No connector available for type: " + ds.dbType());
    }
    List<ColumnMetadata> columns =
        connector.discoverColumns(jdbcUrl, ds.username(), rawPassword, sqlText);
    List<List<Object>> rows =
        connector.executePreview(jdbcUrl, ds.username(), rawPassword, sqlText, maxRows);
    return new PreviewResult(columns, rows);
  }

  @Override
  public PreviewResult executeWithParams(
      UUID dataSourceId, String sqlText, List<Object> paramValues, int pageSize, int offset) {
    DataSource ds =
        repository
            .findById(dataSourceId)
            .orElseThrow(() -> new DataSourceNotFoundException(dataSourceId));
    String rawPassword = encryption.decrypt(ds.secretRef());
    String jdbcUrl = buildJdbcUrl(ds.dbType(), ds.host(), ds.port(), ds.databaseOrService());
    DbConnectorPort connector = connectors.get(ds.dbType());
    if (connector == null) {
      throw new IllegalStateException("No connector available for type: " + ds.dbType());
    }
    return connector.executeWithParams(
        jdbcUrl, ds.username(), rawPassword, sqlText, paramValues, pageSize, offset);
  }

  private DataSourceView toView(DataSource ds) {
    return new DataSourceView(
        ds.id(),
        ds.name(),
        ds.dbType(),
        ds.host(),
        ds.port(),
        ds.databaseOrService(),
        ds.username(),
        ds.sslMode(),
        ds.status(),
        ds.createdBy(),
        ds.createdAt());
  }

  private String buildJdbcUrl(DataSourceType type, String host, int port, String db) {
    return switch (type) {
      case POSTGRESQL -> "jdbc:postgresql://%s:%d/%s".formatted(host, port, db);
      case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(host, port, db);
      case ORACLE -> "jdbc:oracle:thin:@%s:%d:%s".formatted(host, port, db);
    };
  }
}

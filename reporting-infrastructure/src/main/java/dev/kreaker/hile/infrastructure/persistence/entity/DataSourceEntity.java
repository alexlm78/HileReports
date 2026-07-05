package dev.kreaker.hile.infrastructure.persistence.entity;

import dev.kreaker.hile.domain.datasource.DataSourceStatus;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "data_source")
public class DataSourceEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(name = "db_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private DataSourceType dbType;

  @Column(nullable = false)
  private String host;

  @Column(nullable = false)
  private Integer port;

  @Column(name = "database_or_service", nullable = false)
  private String databaseOrService;

  @Column(nullable = false)
  private String username;

  @Column(name = "secret_ref", nullable = false)
  private String secretRef;

  @Column(name = "ssl_mode")
  private String sslMode;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private DataSourceStatus status;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected DataSourceEntity() {}

  public DataSourceEntity(
      UUID id,
      String name,
      DataSourceType dbType,
      String host,
      Integer port,
      String databaseOrService,
      String username,
      String secretRef,
      String sslMode,
      DataSourceStatus status,
      String createdBy,
      OffsetDateTime createdAt) {
    this.id = id;
    this.name = name;
    this.dbType = dbType;
    this.host = host;
    this.port = port;
    this.databaseOrService = databaseOrService;
    this.username = username;
    this.secretRef = secretRef;
    this.sslMode = sslMode;
    this.status = status;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public DataSourceType getDbType() {
    return dbType;
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getDatabaseOrService() {
    return databaseOrService;
  }

  public String getUsername() {
    return username;
  }

  public String getSecretRef() {
    return secretRef;
  }

  public String getSslMode() {
    return sslMode;
  }

  public DataSourceStatus getStatus() {
    return status;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public void setDatabaseOrService(String databaseOrService) {
    this.databaseOrService = databaseOrService;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setSecretRef(String secretRef) {
    this.secretRef = secretRef;
  }

  public void setSslMode(String sslMode) {
    this.sslMode = sslMode;
  }
}

package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "datasource_access")
public class DataSourceAccessEntity {

  @EmbeddedId private DataSourceAccessId id;

  @Column(name = "granted_at", nullable = false)
  private OffsetDateTime grantedAt;

  protected DataSourceAccessEntity() {}

  public DataSourceAccessEntity(DataSourceAccessId id, OffsetDateTime grantedAt) {
    this.id = id;
    this.grantedAt = grantedAt;
  }

  public DataSourceAccessId getId() {
    return id;
  }

  public OffsetDateTime getGrantedAt() {
    return grantedAt;
  }
}

package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class DataSourceAccessId implements Serializable {

  @Column(name = "datasource_id", columnDefinition = "uuid")
  private UUID datasourceId;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  protected DataSourceAccessId() {}

  public DataSourceAccessId(UUID datasourceId, UUID userId) {
    this.datasourceId = datasourceId;
    this.userId = userId;
  }

  public UUID getDatasourceId() {
    return datasourceId;
  }

  public UUID getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DataSourceAccessId that)) return false;
    return Objects.equals(datasourceId, that.datasourceId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(datasourceId, userId);
  }
}

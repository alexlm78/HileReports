package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_export")
public class ReportExportEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "execution_id", nullable = false, columnDefinition = "uuid")
  private UUID executionId;

  @Column(nullable = false)
  private String format;

  @Column(name = "storage_path", nullable = false, columnDefinition = "text")
  private String storagePath;

  @Column(nullable = false)
  private String status;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;

  protected ReportExportEntity() {}

  public ReportExportEntity(
      UUID id,
      UUID executionId,
      String format,
      String storagePath,
      String status,
      OffsetDateTime expiresAt) {
    this.id = id;
    this.executionId = executionId;
    this.format = format;
    this.storagePath = storagePath;
    this.status = status;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getExecutionId() {
    return executionId;
  }

  public String getFormat() {
    return format;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public String getStatus() {
    return status;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
}

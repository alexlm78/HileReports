package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_version")
public class ReportVersionEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "report_definition_id", nullable = false, columnDefinition = "uuid")
  private UUID reportDefinitionId;

  @Column(name = "version_number", nullable = false)
  private int versionNumber;

  @Column(name = "sql_text", nullable = false, columnDefinition = "text")
  private String sqlText;

  @Column(name = "sql_hash", nullable = false)
  private String sqlHash;

  @Column(name = "validation_status", nullable = false)
  private String validationStatus;

  @Column(name = "preview_status", nullable = false)
  private String previewStatus;

  @Column(name = "max_rows", nullable = false)
  private int maxRows;

  @Column(name = "timeout_seconds", nullable = false)
  private int timeoutSeconds;

  @Column(name = "execution_mode", nullable = false)
  private String executionMode;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ReportVersionEntity() {}

  public ReportVersionEntity(
      UUID id,
      UUID reportDefinitionId,
      int versionNumber,
      String sqlText,
      String sqlHash,
      String validationStatus,
      String previewStatus,
      int maxRows,
      int timeoutSeconds,
      String executionMode,
      String createdBy,
      OffsetDateTime createdAt) {
    this.id = id;
    this.reportDefinitionId = reportDefinitionId;
    this.versionNumber = versionNumber;
    this.sqlText = sqlText;
    this.sqlHash = sqlHash;
    this.validationStatus = validationStatus;
    this.previewStatus = previewStatus;
    this.maxRows = maxRows;
    this.timeoutSeconds = timeoutSeconds;
    this.executionMode = executionMode;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReportDefinitionId() {
    return reportDefinitionId;
  }

  public int getVersionNumber() {
    return versionNumber;
  }

  public String getSqlText() {
    return sqlText;
  }

  public String getSqlHash() {
    return sqlHash;
  }

  public String getValidationStatus() {
    return validationStatus;
  }

  public String getPreviewStatus() {
    return previewStatus;
  }

  public void setPreviewStatus(String previewStatus) {
    this.previewStatus = previewStatus;
  }

  public int getMaxRows() {
    return maxRows;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }

  public String getExecutionMode() {
    return executionMode;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}

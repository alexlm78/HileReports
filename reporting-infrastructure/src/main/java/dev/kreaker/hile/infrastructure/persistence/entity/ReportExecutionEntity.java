package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_execution")
public class ReportExecutionEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "report_definition_id", nullable = false, columnDefinition = "uuid")
  private UUID reportDefinitionId;

  @Column(name = "report_version_id", nullable = false, columnDefinition = "uuid")
  private UUID reportVersionId;

  @Column(name = "requested_by", nullable = false)
  private String requestedBy;

  @Column(name = "requested_at", nullable = false)
  private OffsetDateTime requestedAt;

  @Column(nullable = false)
  private String status;

  @Column(name = "execution_mode", nullable = false)
  private String executionMode;

  @Column(name = "row_count")
  private Long rowCount;

  @Column(name = "duration_ms")
  private Long durationMs;

  @Column(name = "error_code")
  private String errorCode;

  @Column(name = "error_message_sanitized", columnDefinition = "text")
  private String errorMessageSanitized;

  @Column(name = "correlation_id", nullable = false)
  private String correlationId;

  protected ReportExecutionEntity() {}

  public ReportExecutionEntity(
      UUID id,
      UUID reportDefinitionId,
      UUID reportVersionId,
      String requestedBy,
      OffsetDateTime requestedAt,
      String status,
      String executionMode,
      Long rowCount,
      Long durationMs,
      String errorCode,
      String errorMessageSanitized,
      String correlationId) {
    this.id = id;
    this.reportDefinitionId = reportDefinitionId;
    this.reportVersionId = reportVersionId;
    this.requestedBy = requestedBy;
    this.requestedAt = requestedAt;
    this.status = status;
    this.executionMode = executionMode;
    this.rowCount = rowCount;
    this.durationMs = durationMs;
    this.errorCode = errorCode;
    this.errorMessageSanitized = errorMessageSanitized;
    this.correlationId = correlationId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getReportDefinitionId() {
    return reportDefinitionId;
  }

  public UUID getReportVersionId() {
    return reportVersionId;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public OffsetDateTime getRequestedAt() {
    return requestedAt;
  }

  public String getStatus() {
    return status;
  }

  public String getExecutionMode() {
    return executionMode;
  }

  public Long getRowCount() {
    return rowCount;
  }

  public Long getDurationMs() {
    return durationMs;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorMessageSanitized() {
    return errorMessageSanitized;
  }

  public String getCorrelationId() {
    return correlationId;
  }
}

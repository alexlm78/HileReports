package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_definition")
public class ReportDefinitionEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column private String description;

  @Column(name = "category_id", columnDefinition = "uuid")
  private UUID categoryId;

  @Column(name = "data_source_id", nullable = false, columnDefinition = "uuid")
  private UUID dataSourceId;

  @Column(name = "owner_team")
  private String ownerTeam;

  @Column(nullable = false)
  private String status;

  @Column(name = "current_version_id", columnDefinition = "uuid")
  private UUID currentVersionId;

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ReportDefinitionEntity() {}

  public ReportDefinitionEntity(
      UUID id,
      String name,
      String description,
      UUID categoryId,
      UUID dataSourceId,
      String ownerTeam,
      String status,
      String createdBy,
      OffsetDateTime createdAt) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.categoryId = categoryId;
    this.dataSourceId = dataSourceId;
    this.ownerTeam = ownerTeam;
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

  public String getDescription() {
    return description;
  }

  public UUID getCategoryId() {
    return categoryId;
  }

  public UUID getDataSourceId() {
    return dataSourceId;
  }

  public String getOwnerTeam() {
    return ownerTeam;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public UUID getCurrentVersionId() {
    return currentVersionId;
  }

  public void setCurrentVersionId(UUID currentVersionId) {
    this.currentVersionId = currentVersionId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}

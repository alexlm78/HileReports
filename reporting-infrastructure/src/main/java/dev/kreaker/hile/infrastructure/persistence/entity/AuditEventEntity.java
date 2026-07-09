package dev.kreaker.hile.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 100)
  private String actor;

  @Column(name = "action", nullable = false, length = 100)
  private String action;

  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  @Column(name = "entity_id", columnDefinition = "uuid")
  private UUID entityId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "payload_json", columnDefinition = "jsonb")
  private String payloadJson;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected AuditEventEntity() {}

  public AuditEventEntity(
      UUID id,
      String actor,
      String action,
      String entityType,
      UUID entityId,
      String payloadJson,
      OffsetDateTime createdAt) {
    this.id = id;
    this.actor = actor;
    this.action = action;
    this.entityType = entityType;
    this.entityId = entityId;
    this.payloadJson = payloadJson;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getActor() {
    return actor;
  }

  public String getAction() {
    return action;
  }

  public String getEntityType() {
    return entityType;
  }

  public UUID getEntityId() {
    return entityId;
  }

  public String getPayloadJson() {
    return payloadJson;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}

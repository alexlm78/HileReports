package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.dto.AuditEventView;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.infrastructure.persistence.entity.AuditEventEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.AuditEventJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class AuditEventRepositoryAdapter implements AuditEventPort {

  private static final Logger log = LoggerFactory.getLogger(AuditEventRepositoryAdapter.class);

  private final AuditEventJpaRepository jpa;

  public AuditEventRepositoryAdapter(AuditEventJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public void record(
      String actor, String action, String entityType, UUID entityId, String payloadJson) {
    try {
      jpa.save(
          new AuditEventEntity(
              UUID.randomUUID(),
              actor,
              action,
              entityType,
              entityId,
              payloadJson,
              OffsetDateTime.now()));
    } catch (Exception e) {
      log.warn("Audit record failed: actor={} action={} entity={}", actor, action, entityType, e);
    }
  }

  @Override
  public List<AuditEventView> findEvents(String actor, String action, int limit) {
    PageRequest page = PageRequest.of(0, limit);
    List<AuditEventEntity> entities;
    if (actor != null && action != null) {
      entities = jpa.findByActorAndActionOrderByCreatedAtDesc(actor, action, page);
    } else if (actor != null) {
      entities = jpa.findByActorOrderByCreatedAtDesc(actor, page);
    } else if (action != null) {
      entities = jpa.findByActionOrderByCreatedAtDesc(action, page);
    } else {
      entities = jpa.findByOrderByCreatedAtDesc(page);
    }
    return entities.stream().map(this::toView).toList();
  }

  private AuditEventView toView(AuditEventEntity e) {
    return new AuditEventView(
        e.getId(),
        e.getActor(),
        e.getAction(),
        e.getEntityType(),
        e.getEntityId(),
        e.getPayloadJson(),
        e.getCreatedAt());
  }
}

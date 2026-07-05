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
  public List<AuditEventView> findEvents(String actor, String action, int page, int limit) {
    PageRequest pageable = PageRequest.of(page, limit);
    if (actor != null && action != null) {
      return jpa.findByActorAndActionOrderByCreatedAtDesc(actor, action, pageable).stream()
          .map(this::toView)
          .toList();
    } else if (actor != null) {
      return jpa.findByActorOrderByCreatedAtDesc(actor, pageable).stream()
          .map(this::toView)
          .toList();
    } else if (action != null) {
      return jpa.findByActionOrderByCreatedAtDesc(action, pageable).stream()
          .map(this::toView)
          .toList();
    } else {
      return jpa.findByOrderByCreatedAtDesc(pageable).stream().map(this::toView).toList();
    }
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

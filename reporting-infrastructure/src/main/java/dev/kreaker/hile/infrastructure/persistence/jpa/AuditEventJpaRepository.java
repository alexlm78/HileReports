package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.AuditEventEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {

  Page<AuditEventEntity> findByOrderByCreatedAtDesc(Pageable pageable);

  Page<AuditEventEntity> findByActorOrderByCreatedAtDesc(String actor, Pageable pageable);

  Page<AuditEventEntity> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

  Page<AuditEventEntity> findByActorAndActionOrderByCreatedAtDesc(
      String actor, String action, Pageable pageable);
}

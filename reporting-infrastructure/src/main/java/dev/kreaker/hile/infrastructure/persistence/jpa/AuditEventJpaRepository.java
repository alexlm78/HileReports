package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.AuditEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventEntity, UUID> {

  List<AuditEventEntity> findByOrderByCreatedAtDesc(Pageable pageable);

  List<AuditEventEntity> findByActorOrderByCreatedAtDesc(String actor, Pageable pageable);

  List<AuditEventEntity> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

  List<AuditEventEntity> findByActorAndActionOrderByCreatedAtDesc(
      String actor, String action, Pageable pageable);
}

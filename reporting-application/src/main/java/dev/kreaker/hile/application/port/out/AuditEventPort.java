package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.AuditEventView;
import java.util.List;
import java.util.UUID;

public interface AuditEventPort {

  void record(String actor, String action, String entityType, UUID entityId, String payloadJson);

  default void record(String actor, String action, String entityType, UUID entityId) {
    record(actor, action, entityType, entityId, null);
  }

  List<AuditEventView> findEvents(String actor, String action, int page, int limit);

  AuditEventPort NOOP =
      new AuditEventPort() {
        @Override
        public void record(
            String actor, String action, String entityType, UUID entityId, String payloadJson) {}

        @Override
        public List<AuditEventView> findEvents(String actor, String action, int page, int limit) {
          return List.of();
        }
      };
}

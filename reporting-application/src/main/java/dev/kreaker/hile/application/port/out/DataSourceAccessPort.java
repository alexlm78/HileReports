package dev.kreaker.hile.application.port.out;

import java.util.List;
import java.util.UUID;

public interface DataSourceAccessPort {

  void grant(UUID datasourceId, UUID userId);

  void revoke(UUID datasourceId, UUID userId);

  List<UUID> listUserIds(UUID datasourceId);

  boolean isAuthorized(UUID datasourceId, String username);
}

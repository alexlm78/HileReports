package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.DataSourceAccessPort;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessEntity;
import dev.kreaker.hile.infrastructure.persistence.entity.DataSourceAccessId;
import dev.kreaker.hile.infrastructure.persistence.jpa.AppUserJpaRepository;
import dev.kreaker.hile.infrastructure.persistence.jpa.DataSourceAccessJpaRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DataSourceAccessAdapter implements DataSourceAccessPort {

  private final DataSourceAccessJpaRepository repo;
  private final AppUserJpaRepository userRepo;

  public DataSourceAccessAdapter(
      DataSourceAccessJpaRepository repo, AppUserJpaRepository userRepo) {
    this.repo = repo;
    this.userRepo = userRepo;
  }

  @Override
  public void grant(UUID datasourceId, UUID userId) {
    DataSourceAccessId id = new DataSourceAccessId(datasourceId, userId);
    if (!repo.existsById(id)) {
      repo.save(new DataSourceAccessEntity(id, OffsetDateTime.now()));
    }
  }

  @Override
  public void revoke(UUID datasourceId, UUID userId) {
    repo.deleteById(new DataSourceAccessId(datasourceId, userId));
  }

  @Override
  public List<UUID> listUserIds(UUID datasourceId) {
    return repo.findByIdDatasourceId(datasourceId).stream()
        .map(e -> e.getId().getUserId())
        .toList();
  }

  @Override
  public boolean isAuthorized(UUID datasourceId, String username) {
    return userRepo
        .findByUsername(username)
        .map(
            user -> {
              boolean isAdmin =
                  user.getRoles().stream().anyMatch(r -> "PLATFORM_ADMIN".equals(r.getName()));
              if (isAdmin) return true;
              return repo.existsById(new DataSourceAccessId(datasourceId, user.getId()));
            })
        .orElse(false);
  }
}

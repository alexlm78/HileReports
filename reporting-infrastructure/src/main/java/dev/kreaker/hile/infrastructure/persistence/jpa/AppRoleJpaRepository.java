package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.AppRoleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRoleJpaRepository extends JpaRepository<AppRoleEntity, UUID> {

  Optional<AppRoleEntity> findByName(String name);
}

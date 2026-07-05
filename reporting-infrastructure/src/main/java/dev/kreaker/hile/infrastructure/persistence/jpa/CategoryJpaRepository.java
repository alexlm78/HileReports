package dev.kreaker.hile.infrastructure.persistence.jpa;

import dev.kreaker.hile.infrastructure.persistence.entity.CategoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, UUID> {

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, UUID id);
}

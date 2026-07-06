package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.domain.category.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {

  Category save(Category category);

  Optional<Category> findById(UUID id);

  List<Category> findAll();

  PageResult<Category> findAllPaged(int page, int size);

  void deleteById(UUID id);

  boolean existsById(UUID id);

  boolean existsByName(String name);

  boolean existsByNameAndIdNot(String name, UUID excludeId);

  Category update(UUID id, String name, String description);
}

package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.port.out.CategoryRepositoryPort;
import dev.kreaker.hile.domain.category.Category;
import dev.kreaker.hile.infrastructure.persistence.entity.CategoryEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.CategoryJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {

  private final CategoryJpaRepository repo;

  public CategoryRepositoryAdapter(CategoryJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Category save(Category category) {
    return toDomain(
        repo.save(
            new CategoryEntity(
                category.id(),
                category.name(),
                category.description(),
                category.createdBy(),
                category.createdAt())));
  }

  @Override
  public Optional<Category> findById(UUID id) {
    return repo.findById(id).map(this::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return repo.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public PageResult<Category> findAllPaged(int page, int size) {
    Page<CategoryEntity> result = repo.findAll(PageRequest.of(page, size));
    List<Category> content = result.getContent().stream().map(this::toDomain).toList();
    return new PageResult<>(content, page, size, result.getTotalElements());
  }

  @Override
  public void deleteById(UUID id) {
    repo.deleteById(id);
  }

  @Override
  public boolean existsById(UUID id) {
    return repo.existsById(id);
  }

  @Override
  public boolean existsByName(String name) {
    return repo.existsByName(name);
  }

  @Override
  public boolean existsByNameAndIdNot(String name, UUID excludeId) {
    return repo.existsByNameAndIdNot(name, excludeId);
  }

  @Override
  public Category update(UUID id, String name, String description) {
    CategoryEntity entity =
        repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    if (name != null) entity.setName(name);
    if (description != null) entity.setDescription(description);
    return toDomain(repo.save(entity));
  }

  private Category toDomain(CategoryEntity e) {
    return new Category(
        e.getId(), e.getName(), e.getDescription(), e.getCreatedBy(), e.getCreatedAt());
  }
}

package dev.kreaker.hile.infrastructure.persistence;

import dev.kreaker.hile.application.port.out.CategoryRepositoryPort;
import dev.kreaker.hile.domain.category.Category;
import dev.kreaker.hile.infrastructure.persistence.entity.CategoryEntity;
import dev.kreaker.hile.infrastructure.persistence.jpa.CategoryJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  private Category toDomain(CategoryEntity e) {
    return new Category(
        e.getId(), e.getName(), e.getDescription(), e.getCreatedBy(), e.getCreatedAt());
  }
}

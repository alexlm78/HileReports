package dev.kreaker.hile.application.service;

import dev.kreaker.hile.application.dto.CategoryView;
import dev.kreaker.hile.application.dto.CreateCategoryCommand;
import dev.kreaker.hile.application.dto.PageResult;
import dev.kreaker.hile.application.port.in.CategoryUseCase;
import dev.kreaker.hile.application.port.out.CategoryRepositoryPort;
import dev.kreaker.hile.domain.category.Category;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CategoryApplicationService implements CategoryUseCase {

  private final CategoryRepositoryPort repository;

  public CategoryApplicationService(CategoryRepositoryPort repository) {
    this.repository = repository;
  }

  @Override
  public CategoryView create(CreateCategoryCommand command) {
    if (repository.existsByName(command.name())) {
      throw new IllegalArgumentException("Category name already exists: " + command.name());
    }
    Category category =
        new Category(
            UUID.randomUUID(),
            command.name(),
            command.description(),
            command.createdBy(),
            OffsetDateTime.now());
    return toView(repository.save(category));
  }

  @Override
  public List<CategoryView> findAll() {
    return repository.findAll().stream().map(this::toView).toList();
  }

  @Override
  public PageResult<CategoryView> findAllPaged(int page, int size) {
    PageResult<Category> result = repository.findAllPaged(page, size);
    List<CategoryView> content = result.content().stream().map(this::toView).toList();
    return new PageResult<>(content, page, size, result.total());
  }

  @Override
  public CategoryView findById(UUID id) {
    return repository
        .findById(id)
        .map(this::toView)
        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
  }

  @Override
  public CategoryView update(UUID id, String name, String description) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Category not found: " + id);
    }
    if (name != null && repository.existsByNameAndIdNot(name, id)) {
      throw new IllegalArgumentException("Category name already exists: " + name);
    }
    return toView(repository.update(id, name, description));
  }

  @Override
  public void delete(UUID id) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Category not found: " + id);
    }
    repository.deleteById(id);
  }

  private CategoryView toView(Category c) {
    return new CategoryView(c.id(), c.name(), c.description(), c.createdBy(), c.createdAt());
  }
}

package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CategoryView;
import dev.kreaker.hile.application.dto.CreateCategoryCommand;
import dev.kreaker.hile.application.dto.PageResult;
import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {

  CategoryView create(CreateCategoryCommand command);

  List<CategoryView> findAll();

  PageResult<CategoryView> findAllPaged(int page, int size);

  CategoryView findById(UUID id);

  CategoryView update(UUID id, String name, String description);

  void delete(UUID id);
}

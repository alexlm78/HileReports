package dev.kreaker.hile.application.port.in;

import dev.kreaker.hile.application.dto.CategoryView;
import dev.kreaker.hile.application.dto.CreateCategoryCommand;
import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {

  CategoryView create(CreateCategoryCommand command);

  List<CategoryView> findAll();

  CategoryView findById(UUID id);

  void delete(UUID id);
}

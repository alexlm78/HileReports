package dev.kreaker.hile.bootstrap.api.category;

import dev.kreaker.hile.application.dto.CategoryView;
import dev.kreaker.hile.application.dto.CreateCategoryCommand;
import dev.kreaker.hile.application.port.in.CategoryUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final CategoryUseCase categoryUseCase;

  public CategoryController(CategoryUseCase categoryUseCase) {
    this.categoryUseCase = categoryUseCase;
  }

  @PostMapping
  public ResponseEntity<CategoryView> create(
      @Valid @RequestBody CategoryRequest request, Principal principal) {
    CategoryView view =
        categoryUseCase.create(
            new CreateCategoryCommand(request.name(), request.description(), principal.getName()));
    return ResponseEntity.created(URI.create("/api/v1/categories/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<List<CategoryView>> findAll() {
    return ResponseEntity.ok(categoryUseCase.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryView> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(categoryUseCase.findById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    categoryUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}

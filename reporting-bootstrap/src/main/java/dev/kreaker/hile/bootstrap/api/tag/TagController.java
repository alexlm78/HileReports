package dev.kreaker.hile.bootstrap.api.tag;

import dev.kreaker.hile.application.dto.CreateTagCommand;
import dev.kreaker.hile.application.dto.TagView;
import dev.kreaker.hile.application.port.in.TagUseCase;
import jakarta.validation.Valid;
import java.net.URI;
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
@RequestMapping("/api/v1/tags")
public class TagController {

  private final TagUseCase tagUseCase;

  public TagController(TagUseCase tagUseCase) {
    this.tagUseCase = tagUseCase;
  }

  @PostMapping
  public ResponseEntity<TagView> create(@Valid @RequestBody TagRequest request) {
    TagView view = tagUseCase.create(new CreateTagCommand(request.name()));
    return ResponseEntity.created(URI.create("/api/v1/tags/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<List<TagView>> findAll() {
    return ResponseEntity.ok(tagUseCase.findAll());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    tagUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}

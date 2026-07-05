package dev.kreaker.hile.bootstrap.api.datasource;

import dev.kreaker.hile.application.dto.CreateDataSourceCommand;
import dev.kreaker.hile.application.dto.DataSourceView;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
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
@RequestMapping("/api/v1/datasources")
public class DataSourceController {

  private final DataSourceUseCase dataSourceUseCase;

  public DataSourceController(DataSourceUseCase dataSourceUseCase) {
    this.dataSourceUseCase = dataSourceUseCase;
  }

  @PostMapping
  public ResponseEntity<DataSourceView> create(
      @Valid @RequestBody CreateDataSourceRequest request, Principal principal) {
    DataSourceView view =
        dataSourceUseCase.create(
            new CreateDataSourceCommand(
                request.name(),
                request.dbType(),
                request.host(),
                request.port(),
                request.databaseOrService(),
                request.username(),
                request.password(),
                request.sslMode(),
                principal.getName()));
    return ResponseEntity.created(URI.create("/api/v1/datasources/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<List<DataSourceView>> findAll() {
    return ResponseEntity.ok(dataSourceUseCase.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<DataSourceView> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(dataSourceUseCase.findById(id));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    dataSourceUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/test")
  public ResponseEntity<ValidationResult> testConnection(@PathVariable UUID id) {
    return ResponseEntity.ok(dataSourceUseCase.testConnection(id));
  }
}

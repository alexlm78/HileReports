package dev.kreaker.hile.bootstrap.api.datasource;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.CreateDataSourceCommand;
import dev.kreaker.hile.application.dto.DataSourceView;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.UpdateDataSourceCommand;
import dev.kreaker.hile.application.dto.UserView;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.in.DataSourceUseCase;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.bootstrap.api.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/datasources")
public class DataSourceController {

  private final DataSourceUseCase dataSourceUseCase;
  private final AuditEventPort auditEventPort;
  private final int previewMaxRows;

  public DataSourceController(
      DataSourceUseCase dataSourceUseCase,
      AuditEventPort auditEventPort,
      @Value("${hile.reports.preview.max-rows:100}") int previewMaxRows) {
    this.dataSourceUseCase = dataSourceUseCase;
    this.auditEventPort = auditEventPort;
    this.previewMaxRows = previewMaxRows;
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
    auditEventPort.record(principal.getName(), "DATASOURCE_CREATED", "DATASOURCE", view.id());
    return ResponseEntity.created(URI.create("/api/v1/datasources/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<PageResponse<DataSourceView>> findAll(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(PageResponse.from(dataSourceUseCase.findAllPaged(page, size)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<DataSourceView> findById(@PathVariable UUID id) {
    return ResponseEntity.ok(dataSourceUseCase.findById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<DataSourceView> update(
      @PathVariable UUID id, @RequestBody UpdateDataSourceRequest request, Principal principal) {
    DataSourceView view =
        dataSourceUseCase.update(
            new UpdateDataSourceCommand(
                id,
                request.name(),
                request.host(),
                request.port(),
                request.databaseOrService(),
                request.username(),
                request.password(),
                request.sslMode()));
    auditEventPort.record(principal.getName(), "DATASOURCE_UPDATED", "DATASOURCE", id);
    return ResponseEntity.ok(view);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
    dataSourceUseCase.delete(id);
    auditEventPort.record(principal.getName(), "DATASOURCE_DELETED", "DATASOURCE", id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/test")
  public ResponseEntity<ValidationResult> testConnection(@PathVariable UUID id) {
    return ResponseEntity.ok(dataSourceUseCase.testConnection(id));
  }

  @PostMapping("/{id}/discover")
  public ResponseEntity<List<ColumnMetadata>> discoverColumns(
      @PathVariable UUID id, @Valid @RequestBody SqlQueryRequest request) {
    return ResponseEntity.ok(dataSourceUseCase.discoverColumns(id, request.sqlText()));
  }

  @PostMapping("/{id}/preview")
  public ResponseEntity<PreviewResult> executePreview(
      @PathVariable UUID id, @Valid @RequestBody SqlQueryRequest request) {
    return ResponseEntity.ok(
        dataSourceUseCase.executePreview(id, request.sqlText(), previewMaxRows));
  }

  @PostMapping("/{id}/access")
  public ResponseEntity<Void> grantAccess(
      @PathVariable UUID id, @RequestBody AccessGrantRequest request, Principal principal) {
    dataSourceUseCase.grantAccess(id, request.userId());
    auditEventPort.record(principal.getName(), "DATASOURCE_ACCESS_GRANTED", "DATASOURCE", id);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}/access/{userId}")
  public ResponseEntity<Void> revokeAccess(
      @PathVariable UUID id, @PathVariable UUID userId, Principal principal) {
    dataSourceUseCase.revokeAccess(id, userId);
    auditEventPort.record(principal.getName(), "DATASOURCE_ACCESS_REVOKED", "DATASOURCE", id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/access")
  public ResponseEntity<List<UserView>> listAccess(@PathVariable UUID id) {
    return ResponseEntity.ok(dataSourceUseCase.listGrantedUsers(id));
  }
}

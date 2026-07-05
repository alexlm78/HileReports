package dev.kreaker.hile.bootstrap.api.report;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.ReportColumnView;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import dev.kreaker.hile.application.dto.ReportParameterView;
import dev.kreaker.hile.application.dto.TagView;
import dev.kreaker.hile.application.dto.UpdateReportCommand;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import dev.kreaker.hile.application.port.in.TagUseCase;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.bootstrap.api.tag.SetReportTagsRequest;
import dev.kreaker.hile.domain.report.ReportColumn;
import dev.kreaker.hile.domain.report.ReportParameter;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

  private final CreateReportDefinitionUseCase reportUseCase;
  private final TagUseCase tagUseCase;
  private final AuditEventPort auditEventPort;

  public ReportController(
      CreateReportDefinitionUseCase reportUseCase,
      TagUseCase tagUseCase,
      AuditEventPort auditEventPort) {
    this.reportUseCase = reportUseCase;
    this.tagUseCase = tagUseCase;
    this.auditEventPort = auditEventPort;
  }

  @PostMapping
  public ResponseEntity<ReportDefinitionView> createDraft(
      @Valid @RequestBody ReportRequest request, Principal principal) {
    ReportDefinitionView view =
        reportUseCase.createDraft(
            new CreateReportDefinitionCommand(
                request.name(),
                request.description(),
                request.categoryId(),
                request.dataSourceId(),
                request.ownerTeam(),
                request.sqlText(),
                principal.getName()));
    auditEventPort.record(principal.getName(), "REPORT_CREATED", "REPORT", view.id());
    return ResponseEntity.created(URI.create("/api/v1/reports/" + view.id())).body(view);
  }

  @GetMapping
  public ResponseEntity<List<ReportDefinitionView>> findAll() {
    return ResponseEntity.ok(reportUseCase.findAll());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReportDefinitionView> findById(@PathVariable UUID id) {
    return reportUseCase
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<ReportDefinitionView> update(
      @PathVariable UUID id, @RequestBody UpdateReportRequest request, Principal principal) {
    ReportDefinitionView view =
        reportUseCase.updateDraft(
            new UpdateReportCommand(
                id,
                request.name(),
                request.description(),
                request.categoryId(),
                request.dataSourceId(),
                request.ownerTeam(),
                request.sqlText()));
    auditEventPort.record(principal.getName(), "REPORT_UPDATED", "REPORT", id);
    return ResponseEntity.ok(view);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) {
    reportUseCase.deleteDraft(id);
    auditEventPort.record(principal.getName(), "REPORT_DELETED", "REPORT", id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/preview")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<PreviewResult> runPreview(@PathVariable UUID id) {
    return ResponseEntity.ok(reportUseCase.runPreview(id));
  }

  @PostMapping("/{id}/publish")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<ReportDefinitionView> publish(@PathVariable UUID id, Principal principal) {
    ReportDefinitionView view = reportUseCase.publish(id);
    auditEventPort.record(principal.getName(), "REPORT_PUBLISHED", "REPORT", id);
    return ResponseEntity.ok(view);
  }

  @PostMapping("/{id}/unpublish")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<ReportDefinitionView> unpublish(
      @PathVariable UUID id, Principal principal) {
    ReportDefinitionView view = reportUseCase.unpublish(id);
    auditEventPort.record(principal.getName(), "REPORT_UNPUBLISHED", "REPORT", id);
    return ResponseEntity.ok(view);
  }

  @PutMapping("/{id}/columns")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<List<ReportColumnView>> upsertColumns(
      @PathVariable UUID id, @Valid @RequestBody List<ColumnConfigRequest> request) {
    List<ReportColumn> columns =
        request.stream()
            .map(
                r ->
                    new ReportColumn(
                        null,
                        null,
                        r.sourceName(),
                        r.label(),
                        r.dataType(),
                        r.displayType(),
                        r.displayFormat(),
                        r.ordinal(),
                        r.visible(),
                        r.sortable(),
                        r.filterableCandidate()))
            .toList();
    return ResponseEntity.ok(reportUseCase.upsertColumns(id, columns));
  }

  @GetMapping("/{id}/columns")
  public ResponseEntity<List<ReportColumnView>> getColumns(@PathVariable UUID id) {
    return ResponseEntity.ok(reportUseCase.getColumns(id));
  }

  @PutMapping("/{id}/parameters")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<List<ReportParameterView>> upsertParameters(
      @PathVariable UUID id, @Valid @RequestBody List<ParameterConfigRequest> request) {
    List<ReportParameter> params =
        request.stream()
            .map(
                r ->
                    new ReportParameter(
                        null,
                        null,
                        r.name(),
                        r.label(),
                        r.parameterType(),
                        r.operatorType(),
                        r.required(),
                        r.defaultValue(),
                        r.allowsMultiple(),
                        r.sourceColumn(),
                        r.validationRule()))
            .toList();
    return ResponseEntity.ok(reportUseCase.upsertParameters(id, params));
  }

  @GetMapping("/{id}/parameters")
  public ResponseEntity<List<ReportParameterView>> getParameters(@PathVariable UUID id) {
    return ResponseEntity.ok(reportUseCase.getParameters(id));
  }

  @PutMapping("/{id}/tags")
  @PreAuthorize("@reportSecurity.isOwnerOrAdmin(#id, authentication)")
  public ResponseEntity<List<TagView>> setTags(
      @PathVariable UUID id, @Valid @RequestBody SetReportTagsRequest request) {
    tagUseCase.setReportTags(id, request.tagIds());
    return ResponseEntity.ok(tagUseCase.getReportTags(id));
  }

  @GetMapping("/{id}/tags")
  public ResponseEntity<List<TagView>> getTags(@PathVariable UUID id) {
    return ResponseEntity.ok(tagUseCase.getReportTags(id));
  }
}

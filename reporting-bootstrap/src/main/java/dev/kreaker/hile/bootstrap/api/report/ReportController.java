package dev.kreaker.hile.bootstrap.api.report;

import dev.kreaker.hile.application.dto.CreateReportDefinitionCommand;
import dev.kreaker.hile.application.dto.ReportDefinitionView;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

  private final CreateReportDefinitionUseCase reportUseCase;

  public ReportController(CreateReportDefinitionUseCase reportUseCase) {
    this.reportUseCase = reportUseCase;
  }

  @PostMapping
  public ResponseEntity<ReportDefinitionView> createDraft(
      @Valid @RequestBody ReportRequest request, Principal principal) {
    ReportDefinitionView view =
        reportUseCase.createDraft(
            new CreateReportDefinitionCommand(
                request.name(),
                request.description(),
                request.dataSourceId(),
                request.ownerTeam(),
                request.sqlText(),
                principal.getName()));
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
}

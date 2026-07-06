package dev.kreaker.hile.bootstrap.api.report;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;
import dev.kreaker.hile.application.dto.ExecutionView;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import dev.kreaker.hile.bootstrap.api.PageResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ExecuteReportController {

  private final ExecuteReportUseCase executeUseCase;
  private final AuditEventPort auditEventPort;

  public ExecuteReportController(
      ExecuteReportUseCase executeUseCase, AuditEventPort auditEventPort) {
    this.executeUseCase = executeUseCase;
    this.auditEventPort = auditEventPort;
  }

  @PostMapping("/{id}/execute")
  public ResponseEntity<ExecutionResultView> execute(
      @PathVariable UUID id,
      @Valid @RequestBody ExecuteReportRequest request,
      Principal principal) {
    ExecutionResultView result =
        executeUseCase.execute(
            new ExecuteReportCommand(id, request.parameters(), request.page(), request.pageSize()),
            principal.getName());
    auditEventPort.record(principal.getName(), "REPORT_EXECUTED", "REPORT", id);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{id}/executions")
  public ResponseEntity<PageResponse<ExecutionView>> listExecutions(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Principal principal) {
    return ResponseEntity.ok(
        PageResponse.from(executeUseCase.listByReport(id, principal.getName(), page, size)));
  }
}

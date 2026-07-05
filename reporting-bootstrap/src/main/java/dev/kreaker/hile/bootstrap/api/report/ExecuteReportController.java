package dev.kreaker.hile.bootstrap.api.report;

import dev.kreaker.hile.application.dto.ExecuteReportCommand;
import dev.kreaker.hile.application.dto.ExecutionResultView;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ExecuteReportController {

  private final ExecuteReportUseCase executeUseCase;

  public ExecuteReportController(ExecuteReportUseCase executeUseCase) {
    this.executeUseCase = executeUseCase;
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
    return ResponseEntity.ok(result);
  }
}

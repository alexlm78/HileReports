package dev.kreaker.hile.bootstrap.api.execution;

import dev.kreaker.hile.application.dto.ExecutionView;
import dev.kreaker.hile.application.port.in.ExecuteReportUseCase;
import dev.kreaker.hile.bootstrap.api.PageResponse;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/executions")
public class ExecutionController {

  private final ExecuteReportUseCase executeUseCase;

  public ExecutionController(ExecuteReportUseCase executeUseCase) {
    this.executeUseCase = executeUseCase;
  }

  @GetMapping
  public ResponseEntity<PageResponse<ExecutionView>> listMine(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Principal principal) {
    return ResponseEntity.ok(
        PageResponse.from(executeUseCase.listMine(principal.getName(), page, size)));
  }
}

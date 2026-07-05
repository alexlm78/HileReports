package dev.kreaker.hile.bootstrap.api.audit;

import dev.kreaker.hile.application.dto.AuditEventView;
import dev.kreaker.hile.application.port.out.AuditEventPort;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditController {

  private final AuditEventPort auditEventPort;

  public AuditController(AuditEventPort auditEventPort) {
    this.auditEventPort = auditEventPort;
  }

  @GetMapping
  public ResponseEntity<List<AuditEventView>> findEvents(
      @RequestParam(required = false) String actor,
      @RequestParam(required = false) String action,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int limit) {
    return ResponseEntity.ok(auditEventPort.findEvents(actor, action, page, Math.min(limit, 1000)));
  }
}

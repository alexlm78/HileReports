package dev.kreaker.hile.bootstrap.api.catalog;

import dev.kreaker.hile.application.dto.CatalogReportView;
import dev.kreaker.hile.application.port.in.CreateReportDefinitionUseCase;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

  private final CreateReportDefinitionUseCase reportUseCase;

  public CatalogController(CreateReportDefinitionUseCase reportUseCase) {
    this.reportUseCase = reportUseCase;
  }

  @GetMapping
  public ResponseEntity<List<CatalogReportView>> getCatalog(
      @RequestParam(required = false) String name) {
    return ResponseEntity.ok(reportUseCase.getCatalog(name));
  }
}

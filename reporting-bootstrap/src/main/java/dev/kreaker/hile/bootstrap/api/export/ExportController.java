package dev.kreaker.hile.bootstrap.api.export;

import dev.kreaker.hile.application.dto.ExportJobCreationResult;
import dev.kreaker.hile.application.dto.ExportJobView;
import dev.kreaker.hile.application.dto.RequestExportCommand;
import dev.kreaker.hile.application.port.in.ExportJobUseCase;
import dev.kreaker.hile.bootstrap.api.PageResponse;
import dev.kreaker.hile.bootstrap.export.AsyncExportService;
import jakarta.validation.Valid;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportController {

  private final ExportJobUseCase exportUseCase;
  private final AsyncExportService asyncExportService;

  public ExportController(ExportJobUseCase exportUseCase, AsyncExportService asyncExportService) {
    this.exportUseCase = exportUseCase;
    this.asyncExportService = asyncExportService;
  }

  @PostMapping("/api/v1/reports/{id}/export")
  public ResponseEntity<ExportJobView> requestExport(
      @PathVariable UUID id, @Valid @RequestBody ExportRequest request, Principal principal) {
    ExportJobCreationResult result =
        exportUseCase.requestExport(
            new RequestExportCommand(
                id, request.format(), request.parameters(), request.pageSize()),
            principal.getName());
    asyncExportService.processAsync(result.task());
    return ResponseEntity.accepted()
        .location(URI.create("/api/v1/exports/" + result.view().id()))
        .body(result.view());
  }

  @GetMapping("/api/v1/exports")
  public ResponseEntity<PageResponse<ExportJobView>> listExports(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      Principal principal) {
    return ResponseEntity.ok(
        PageResponse.from(exportUseCase.listExports(principal.getName(), page, size)));
  }

  @GetMapping("/api/v1/exports/{exportId}")
  public ResponseEntity<ExportJobView> getExport(@PathVariable UUID exportId) {
    return ResponseEntity.ok(exportUseCase.getExport(exportId));
  }

  @GetMapping("/api/v1/exports/{exportId}/download")
  public ResponseEntity<Resource> download(@PathVariable UUID exportId) {
    String filePath;
    try {
      filePath = exportUseCase.getExportFilePath(exportId);
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (IllegalArgumentException e) {
      return ResponseEntity.notFound().build();
    }

    Path path = Path.of(filePath);
    if (!Files.exists(path)) {
      return ResponseEntity.notFound().build();
    }

    String filename = path.getFileName().toString();
    String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    MediaType contentType =
        "csv".equals(ext)
            ? MediaType.parseMediaType("text/csv")
            : MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    return ResponseEntity.ok()
        .contentType(contentType)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(new FileSystemResource(path));
  }
}

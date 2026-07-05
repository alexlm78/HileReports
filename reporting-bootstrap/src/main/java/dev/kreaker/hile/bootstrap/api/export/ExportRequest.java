package dev.kreaker.hile.bootstrap.api.export;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;

public record ExportRequest(
    @NotBlank @Pattern(regexp = "CSV|XLSX", message = "format must be CSV or XLSX") String format,
    Map<String, String> parameters,
    @Min(1) @Max(100000) int pageSize) {

  public ExportRequest {
    if (parameters == null) parameters = Map.of();
    if (pageSize <= 0) pageSize = 5000;
  }
}

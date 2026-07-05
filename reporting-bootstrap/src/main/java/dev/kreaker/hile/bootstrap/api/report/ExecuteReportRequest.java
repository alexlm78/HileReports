package dev.kreaker.hile.bootstrap.api.report;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;

public record ExecuteReportRequest(
    Map<String, String> parameters, @Min(0) int page, @Min(1) @Max(1000) int pageSize) {

  public ExecuteReportRequest {
    if (parameters == null) parameters = Map.of();
    if (pageSize <= 0) pageSize = 100;
  }
}

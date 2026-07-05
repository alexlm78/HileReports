package dev.kreaker.hile.application.dto;

import java.util.Map;
import java.util.UUID;

public record ExecuteReportCommand(
    UUID reportId, Map<String, String> parameterValues, int page, int pageSize) {}

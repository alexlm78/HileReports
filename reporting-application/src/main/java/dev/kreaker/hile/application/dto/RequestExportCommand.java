package dev.kreaker.hile.application.dto;

import java.util.Map;
import java.util.UUID;

public record RequestExportCommand(
    UUID reportId, String format, Map<String, String> parameterValues, int pageSize) {}

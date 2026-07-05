package dev.kreaker.hile.application.dto;

import java.util.List;
import java.util.UUID;

public record ExecutionResultView(
    UUID executionId,
    String correlationId,
    List<ColumnMetadata> columns,
    List<List<Object>> rows,
    long rowCount,
    long durationMs,
    int page,
    int pageSize) {}

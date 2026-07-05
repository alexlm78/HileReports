package dev.kreaker.hile.application.dto;

import java.util.List;
import java.util.UUID;

public record AsyncExportTask(
    UUID exportId,
    UUID executionId,
    UUID reportDefinitionId,
    UUID reportVersionId,
    UUID dataSourceId,
    String boundSql,
    List<Object> paramValues,
    int pageSize,
    String format,
    String storagePath) {}

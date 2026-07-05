package dev.kreaker.hile.application.dto;

import java.util.List;

public record PreviewResult(List<ColumnMetadata> columns, List<List<Object>> rows) {}

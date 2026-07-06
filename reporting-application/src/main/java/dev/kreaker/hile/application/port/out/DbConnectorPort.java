package dev.kreaker.hile.application.port.out;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.PreviewResult;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.util.List;

public interface DbConnectorPort {

  boolean supports(DataSourceType dataSourceType);

  ValidationResult testConnection(String jdbcUrl, String username, String rawPassword);

  List<ColumnMetadata> discoverColumns(
      String jdbcUrl, String username, String rawPassword, String sqlText);

  List<List<Object>> executePreview(
      String jdbcUrl, String username, String rawPassword, String sqlText, int limit);

  PreviewResult executeWithParams(
      String jdbcUrl,
      String username,
      String rawPassword,
      String sqlText,
      List<Object> paramValues,
      int pageSize,
      int offset);
}

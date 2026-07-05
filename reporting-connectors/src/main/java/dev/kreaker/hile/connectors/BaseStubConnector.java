package dev.kreaker.hile.connectors;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.util.List;

abstract class BaseStubConnector implements DbConnectorPort {

  @Override
  public ValidationResult testConnection(String jdbcUrl, String username, String rawPassword) {
    if (jdbcUrl == null || jdbcUrl.isBlank()) {
      return ValidationResult.failure("jdbcUrl is required.");
    }
    return ValidationResult.ok();
  }

  @Override
  public List<ColumnMetadata> discoverColumns(
      String jdbcUrl, String username, String rawPassword, String sqlText) {
    return List.of(new ColumnMetadata("sample_column", "sample_column", "string"));
  }

  @Override
  public List<List<Object>> executePreview(
      String jdbcUrl, String username, String rawPassword, String sqlText, int limit) {
    return List.of(List.of(supportsTypeName(), "preview"));
  }

  protected abstract String supportsTypeName();

  @Override
  public abstract boolean supports(DataSourceType dataSourceType);
}

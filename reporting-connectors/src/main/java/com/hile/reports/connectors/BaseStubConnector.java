package com.hile.reports.connectors;

import com.hile.reports.application.dto.ColumnMetadata;
import com.hile.reports.application.dto.ValidationResult;
import com.hile.reports.application.port.out.DbConnectorPort;
import com.hile.reports.domain.datasource.DataSourceType;
import java.util.List;

abstract class BaseStubConnector implements DbConnectorPort {

  @Override
  public ValidationResult testConnection(String jdbcUrl, String username, String encryptedSecret) {
    if (jdbcUrl == null || jdbcUrl.isBlank()) {
      return ValidationResult.failure("El jdbcUrl es obligatorio.");
    }
    return ValidationResult.ok();
  }

  @Override
  public List<ColumnMetadata> discoverColumns(String sqlText) {
    return List.of(new ColumnMetadata("sample_column", "sample_column", "string"));
  }

  @Override
  public List<List<Object>> executePreview(String sqlText, int limit) {
    return List.of(List.of(supportsTypeName(), "preview"));
  }

  protected abstract String supportsTypeName();

  @Override
  public abstract boolean supports(DataSourceType dataSourceType);
}

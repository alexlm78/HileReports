package dev.kreaker.hile.connectors;

import dev.kreaker.hile.domain.datasource.DataSourceType;

public class PostgreSqlConnectorAdapter extends BaseStubConnector {

  @Override
  public boolean supports(DataSourceType dataSourceType) {
    return dataSourceType == DataSourceType.POSTGRESQL;
  }

  @Override
  protected String supportsTypeName() {
    return "postgresql";
  }
}

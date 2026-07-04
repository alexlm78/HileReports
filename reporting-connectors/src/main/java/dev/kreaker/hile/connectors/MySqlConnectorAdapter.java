package dev.kreaker.hile.connectors;

import dev.kreaker.hile.domain.datasource.DataSourceType;

public class MySqlConnectorAdapter extends BaseStubConnector {

  @Override
  public boolean supports(DataSourceType dataSourceType) {
    return dataSourceType == DataSourceType.MYSQL;
  }

  @Override
  protected String supportsTypeName() {
    return "mysql";
  }
}

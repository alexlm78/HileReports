package com.hile.reports.connectors;

import com.hile.reports.domain.datasource.DataSourceType;

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

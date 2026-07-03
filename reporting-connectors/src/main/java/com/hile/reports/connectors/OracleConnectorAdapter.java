package com.hile.reports.connectors;

import com.hile.reports.domain.datasource.DataSourceType;

public class OracleConnectorAdapter extends BaseStubConnector {

  @Override
  public boolean supports(DataSourceType dataSourceType) {
    return dataSourceType == DataSourceType.ORACLE;
  }

  @Override
  protected String supportsTypeName() {
    return "oracle";
  }
}

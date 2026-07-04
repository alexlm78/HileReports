package dev.kreaker.hile.connectors;

import dev.kreaker.hile.domain.datasource.DataSourceType;

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

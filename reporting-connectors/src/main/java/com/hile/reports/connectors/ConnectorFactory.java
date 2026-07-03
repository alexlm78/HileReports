package com.hile.reports.connectors;

import com.hile.reports.application.port.out.DbConnectorPort;
import com.hile.reports.domain.datasource.DataSourceType;
import java.util.List;

public class ConnectorFactory {

  private final List<DbConnectorPort> connectors;

  public ConnectorFactory(List<DbConnectorPort> connectors) {
    this.connectors = connectors;
  }

  public DbConnectorPort forType(DataSourceType dataSourceType) {
    return connectors.stream()
        .filter(connector -> connector.supports(dataSourceType))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("No existe conector para " + dataSourceType));
  }
}

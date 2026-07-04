package dev.kreaker.hile.connectors;

import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.domain.datasource.DataSourceType;
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

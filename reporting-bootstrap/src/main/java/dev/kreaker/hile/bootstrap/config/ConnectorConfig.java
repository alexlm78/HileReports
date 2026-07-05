package dev.kreaker.hile.bootstrap.config;

import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.connectors.ConnectorFactory;
import dev.kreaker.hile.connectors.MySqlConnectorAdapter;
import dev.kreaker.hile.connectors.OracleConnectorAdapter;
import dev.kreaker.hile.connectors.PostgreSqlConnectorAdapter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectorConfig {

  @Bean
  DbConnectorPort postgreSqlConnector() {
    return new PostgreSqlConnectorAdapter();
  }

  @Bean
  DbConnectorPort mySqlConnector() {
    return new MySqlConnectorAdapter();
  }

  @Bean
  DbConnectorPort oracleConnector() {
    return new OracleConnectorAdapter();
  }

  @Bean
  ConnectorFactory connectorFactory(List<DbConnectorPort> connectors) {
    return new ConnectorFactory(connectors);
  }
}

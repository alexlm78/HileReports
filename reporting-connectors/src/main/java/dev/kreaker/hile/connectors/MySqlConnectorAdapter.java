package dev.kreaker.hile.connectors;

import dev.kreaker.hile.application.dto.ColumnMetadata;
import dev.kreaker.hile.application.dto.ValidationResult;
import dev.kreaker.hile.application.port.out.DbConnectorPort;
import dev.kreaker.hile.domain.datasource.DataSourceType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlConnectorAdapter implements DbConnectorPort {

  @Override
  public boolean supports(DataSourceType dataSourceType) {
    return dataSourceType == DataSourceType.MYSQL;
  }

  @Override
  public ValidationResult testConnection(String jdbcUrl, String username, String rawPassword) {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, username, rawPassword)) {
      conn.isValid(5);
      return ValidationResult.ok();
    } catch (SQLException e) {
      return ValidationResult.failure("Connection failed: " + e.getMessage());
    }
  }

  @Override
  public List<ColumnMetadata> discoverColumns(
      String jdbcUrl, String username, String rawPassword, String sqlText) {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, username, rawPassword);
        PreparedStatement ps = conn.prepareStatement(sqlText)) {
      ps.setMaxRows(1);
      try (ResultSet rs = ps.executeQuery()) {
        return extractColumnMetadata(rs.getMetaData());
      }
    } catch (SQLException e) {
      throw new RuntimeException("Column discovery failed: " + e.getMessage(), e);
    }
  }

  @Override
  public List<List<Object>> executePreview(
      String jdbcUrl, String username, String rawPassword, String sqlText, int limit) {
    try (Connection conn = DriverManager.getConnection(jdbcUrl, username, rawPassword);
        PreparedStatement ps = conn.prepareStatement(sqlText)) {
      ps.setMaxRows(limit);
      try (ResultSet rs = ps.executeQuery()) {
        int cols = rs.getMetaData().getColumnCount();
        List<List<Object>> rows = new ArrayList<>();
        while (rs.next()) {
          List<Object> row = new ArrayList<>(cols);
          for (int i = 1; i <= cols; i++) {
            row.add(rs.getObject(i));
          }
          rows.add(row);
        }
        return rows;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Preview execution failed: " + e.getMessage(), e);
    }
  }

  private List<ColumnMetadata> extractColumnMetadata(ResultSetMetaData meta) throws SQLException {
    int cols = meta.getColumnCount();
    List<ColumnMetadata> result = new ArrayList<>(cols);
    for (int i = 1; i <= cols; i++) {
      result.add(
          new ColumnMetadata(
              meta.getColumnName(i),
              meta.getColumnLabel(i),
              meta.getColumnTypeName(i).toLowerCase()));
    }
    return result;
  }
}

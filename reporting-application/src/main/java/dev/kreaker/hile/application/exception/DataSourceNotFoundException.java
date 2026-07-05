package dev.kreaker.hile.application.exception;

import java.util.UUID;

public class DataSourceNotFoundException extends RuntimeException {

  public DataSourceNotFoundException(UUID id) {
    super("DataSource not found: " + id);
  }
}

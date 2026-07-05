package dev.kreaker.hile.bootstrap.api.datasource;

public record UpdateDataSourceRequest(
    String name,
    String host,
    Integer port,
    String databaseOrService,
    String username,
    String password,
    String sslMode) {}

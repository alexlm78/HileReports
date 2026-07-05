package dev.kreaker.hile.application.dto;

import java.util.UUID;

public record UpdateDataSourceCommand(
    UUID id,
    String name,
    String host,
    Integer port,
    String databaseOrService,
    String username,
    String password,
    String sslMode) {}

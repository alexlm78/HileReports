package dev.kreaker.hile.application.dto;

public record CreateUserCommand(String username, String password, String email, String role) {}

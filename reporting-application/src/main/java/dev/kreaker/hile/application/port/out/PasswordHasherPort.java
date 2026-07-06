package dev.kreaker.hile.application.port.out;

public interface PasswordHasherPort {

  String hash(String rawPassword);
}

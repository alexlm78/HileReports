package dev.kreaker.hile.application.port.out;

public interface PasswordEncryptionPort {

  String encrypt(String plaintext);

  String decrypt(String encoded);
}

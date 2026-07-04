package dev.kreaker.hile.bootstrap.security;

import dev.kreaker.hile.application.port.out.UserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SecurityDataInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SecurityDataInitializer.class);

  private final UserRepositoryPort userRepositoryPort;
  private final PasswordEncoder passwordEncoder;

  public SecurityDataInitializer(
      UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
    this.userRepositoryPort = userRepositoryPort;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!userRepositoryPort.existsByUsername("admin")) {
      userRepositoryPort.save("admin", passwordEncoder.encode("admin123"), "PLATFORM_ADMIN");
      log.info("Default admin user created. Change the password immediately.");
    }
  }
}

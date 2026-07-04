package dev.kreaker.hile.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.kreaker.hile")
public class HileReportsApplication {

  public static void main(String[] args) {
    SpringApplication.run(HileReportsApplication.class, args);
  }
}

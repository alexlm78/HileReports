package dev.kreaker.hile.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "dev.kreaker.hile",
      exclude = UserDetailsServiceAutoConfiguration.class)
@EntityScan(basePackages = "dev.kreaker.hile")
public class HileReportsApplication {

   public static void main(String[] args) {
      SpringApplication.run(HileReportsApplication.class, args);
   }
}

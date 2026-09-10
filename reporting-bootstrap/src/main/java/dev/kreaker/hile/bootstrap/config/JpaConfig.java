package dev.kreaker.hile.bootstrap.config;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaRepositories(basePackages = "dev.kreaker.hile")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "dateTimeProvider")
public class JpaConfig {

  @Bean
  AuditorAware<String> auditorProvider() {
    return () -> {
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
        return Optional.empty();
      }
      return Optional.of(auth.getName());
    };
  }

  @Bean
  DateTimeProvider dateTimeProvider() {
    // Default Spring Data auditing provider only produces LocalDateTime, which cannot
    // auto-convert to the OffsetDateTime fields on AuditableEntity (no zone info to attach).
    return () -> Optional.of(OffsetDateTime.now());
  }
}

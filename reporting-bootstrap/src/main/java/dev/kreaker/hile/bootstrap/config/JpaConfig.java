package dev.kreaker.hile.bootstrap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "dev.kreaker.hile")
public class JpaConfig {}

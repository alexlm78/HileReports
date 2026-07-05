package dev.kreaker.hile.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  OpenAPI hileReportsOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("HileReports API")
                .description(
                    "Reporting platform API — datasource management, report design, "
                        + "parameterized execution, async export, audit log.")
                .version("v1")
                .contact(new Contact().name("Kreaker Dev").email("alejandro@kreaker.dev")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
  }
}

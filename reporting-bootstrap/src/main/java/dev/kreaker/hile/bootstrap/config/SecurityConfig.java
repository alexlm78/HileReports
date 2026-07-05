package dev.kreaker.hile.bootstrap.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import dev.kreaker.hile.bootstrap.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(UNAUTHORIZED)))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Public
                    .requestMatchers(
                        "/api/v1/auth/login",
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/prometheus")
                    .permitAll()
                    // Datasource management — PLATFORM_ADMIN only
                    .requestMatchers("/api/v1/datasources/**")
                    .hasRole("PLATFORM_ADMIN")
                    // Category management — PLATFORM_ADMIN only for mutations
                    .requestMatchers(POST, "/api/v1/categories")
                    .hasRole("PLATFORM_ADMIN")
                    .requestMatchers(DELETE, "/api/v1/categories/**")
                    .hasRole("PLATFORM_ADMIN")
                    .requestMatchers("/api/v1/categories/**")
                    .authenticated()
                    // Report creation — PLATFORM_ADMIN or REPORT_DESIGNER
                    .requestMatchers(POST, "/api/v1/reports")
                    .hasAnyRole("PLATFORM_ADMIN", "REPORT_DESIGNER")
                    // Report execution and export — REPORT_EXECUTE permission
                    // (PLATFORM_ADMIN, REPORT_DESIGNER, REPORT_VIEWER)
                    .requestMatchers(POST, "/api/v1/reports/*/execute", "/api/v1/reports/*/export")
                    .hasAnyRole("PLATFORM_ADMIN", "REPORT_DESIGNER", "REPORT_VIEWER")
                    // All other report operations (design) — PLATFORM_ADMIN or REPORT_DESIGNER
                    .requestMatchers("/api/v1/reports/**")
                    .hasAnyRole("PLATFORM_ADMIN", "REPORT_DESIGNER")
                    // Catalog and exports — REPORT_EXECUTE permission
                    .requestMatchers("/api/v1/catalog/**", "/api/v1/exports/**")
                    .hasAnyRole("PLATFORM_ADMIN", "REPORT_DESIGNER", "REPORT_VIEWER")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}

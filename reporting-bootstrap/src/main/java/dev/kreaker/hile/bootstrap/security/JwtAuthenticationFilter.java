package dev.kreaker.hile.bootstrap.security;

import dev.kreaker.hile.security.TokenProviderPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final TokenProviderPort jwtTokenProvider;

  public JwtAuthenticationFilter(TokenProviderPort jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    MDC.put("correlationId", correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    try {
      String token = extractBearerToken(request);
      if (token != null && jwtTokenProvider.isValid(token)) {
        String username = jwtTokenProvider.extractUsername(token);
        List<SimpleGrantedAuthority> authorities =
            jwtTokenProvider.extractRoles(token).stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        MDC.put("username", username);
      }
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }

  private String extractBearerToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}

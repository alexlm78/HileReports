package dev.kreaker.hile.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private static final String SECRET =
      "test-jwt-secret-key-for-unit-tests-min-64-chars-padding-here!!";
  private static final long EXPIRATION_MS = 3600000L;

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    provider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
  }

  @Test
  void shouldGenerateAndValidateToken() {
    String token = provider.generateToken("alice", Set.of("REPORT_VIEWER"));

    assertThat(provider.isValid(token)).isTrue();
  }

  @Test
  void shouldExtractUsernameFromToken() {
    String token = provider.generateToken("alice", Set.of("REPORT_VIEWER"));

    assertThat(provider.extractUsername(token)).isEqualTo("alice");
  }

  @Test
  void shouldExtractRolesFromToken() {
    Set<String> roles = Set.of("PLATFORM_ADMIN", "REPORT_DESIGNER");
    String token = provider.generateToken("bob", roles);

    assertThat(provider.extractRoles(token)).containsExactlyInAnyOrderElementsOf(roles);
  }

  @Test
  void shouldRejectTamperedToken() {
    String token = provider.generateToken("alice", Set.of("REPORT_VIEWER"));
    String tampered = token.substring(0, token.length() - 5) + "XXXXX";

    assertThat(provider.isValid(tampered)).isFalse();
  }

  @Test
  void shouldRejectEmptyToken() {
    assertThat(provider.isValid("")).isFalse();
    assertThat(provider.isValid(null)).isFalse();
  }

  @Test
  void shouldReturnExpirationMs() {
    assertThat(provider.expirationMs()).isEqualTo(EXPIRATION_MS);
  }
}

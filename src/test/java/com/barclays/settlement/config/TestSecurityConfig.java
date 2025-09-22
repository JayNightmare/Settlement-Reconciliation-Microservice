package com.barclays.settlement.config;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@TestConfiguration
public class TestSecurityConfig {

  @Bean
  public JwtDecoder jwtDecoder() {
    return token ->
        Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("sub", "test-user")
            .claim("realm_access", Map.of("roles", List.of("OPS_ANALYST")))
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build();
  }
}

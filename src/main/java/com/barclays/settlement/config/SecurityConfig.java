package com.barclays.settlement.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        HttpMethod.GET,
                        "/actuator/health",
                        "/actuator/info",
                        "/v3/api-docs/**",
                        "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth ->
                oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
    return http.build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    return converter;
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null) {
      return List.of();
    }
    Object roles = realmAccess.get("roles");
    if (!(roles instanceof Collection<?> collection)) {
      return List.of();
    }
    return collection.stream()
        .map(Object::toString)
        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
  }
}

package org.github.bootcamp.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway security: validates JWT on all API routes, passes through the auth server.
 *
 * <p>Only the actuator endpoints meant for probes and scraping are anonymous. This is the public
 * entry point, and CSRF is off, so a blanket {@code /actuator/**} permit hands every exposed
 * endpoint to anyone who can reach the port.
 *
 * @author zhuling
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                .pathMatchers("/oauth2/**", "/.well-known/**").permitAll()
                .pathMatchers("/fallback").permitAll()
                .anyExchange().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {}))
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }
}

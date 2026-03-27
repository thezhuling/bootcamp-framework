package org.github.bootcamp.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Authorization Server.
 * Spring Boot 4.x auto-configures the auth server filter chain (Order 0) via
 * OAuth2AuthorizationServerWebSecurityConfiguration. This class provides Order 2
 * for default form-login and actuator access.
 *
 * @author zhuling
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Default security chain: form login for user authentication (consent screen etc.),
     * actuator endpoints allowed without authentication.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

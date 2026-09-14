package org.github.bootcamp.auth.config;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

/**
 * Security configuration for the Authorization Server.
 *
 * <p>Both filter chains are declared here on purpose. Boot's own authorization server chain is
 * {@code @ConditionalOnDefaultWebSecurity}: it backs off as soon as the application defines any
 * {@link SecurityFilterChain}. With only the form-login chain below, {@code /oauth2/token},
 * {@code /oauth2/jwks} and {@code /.well-known/openid-configuration} were never handled by the
 * authorization server and redirected to {@code /login} instead.
 *
 * @author zhuling
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Protocol endpoints (token, JWKS, OIDC discovery, authorize). Mirrors Boot's
     * {@code OAuth2AuthorizationServerWebSecurityConfiguration}.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .oauth2AuthorizationServer(authorizationServer -> {
                http.securityMatcher(authorizationServer.getEndpointsMatcher());
                authorizationServer.oidc(Customizer.withDefaults());
            })
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
            // browsers hitting /oauth2/authorize unauthenticated go to the login page
            .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                new LoginUrlAuthenticationEntryPoint("/login"), htmlRequests()));
        return http.build();
    }

    /**
     * Everything else: form login for user authentication (consent screen etc.). Only the
     * actuator endpoints meant for probes and scraping are anonymous.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated())
            .formLogin(Customizer.withDefaults())
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static MediaTypeRequestMatcher htmlRequests() {
        var matcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        matcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return matcher;
    }
}

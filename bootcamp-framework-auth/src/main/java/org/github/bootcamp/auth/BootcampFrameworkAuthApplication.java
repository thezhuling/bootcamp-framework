package org.github.bootcamp.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * OAuth2 Authorization Server — issues JWT tokens consumed by all resource servers.
 * Token endpoint: POST http://localhost:9000/oauth2/token
 * JWK Set URI:   GET  http://localhost:9000/oauth2/jwks
 *
 * @author zhuling
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootcampFrameworkAuthApplication.class, args);
    }
}

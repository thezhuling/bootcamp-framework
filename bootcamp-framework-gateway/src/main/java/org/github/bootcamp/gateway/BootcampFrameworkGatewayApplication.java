package org.github.bootcamp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway — routes traffic to downstream services via Nacos lb:// URIs.
 * Features: Token Relay (JWT forwarding), Circuit Breaker.
 *
 * @author zhuling
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootcampFrameworkGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder b) {
        return b.routes()
            // Microservice: REST APIs (excluding AI)
            .route("microservice", r -> r.path("/api/v1/**")
                .and().not(p -> p.path("/api/v1/ai/**"))
                .filters(f -> f
                    .tokenRelay()
                    .circuitBreaker(cb -> cb.setName("microservice-cb")
                        .setFallbackUri("forward:/fallback")))
                .uri("lb://bootcamp-framework-microservice"))
            // AI service
            .route("ai", r -> r.path("/api/v1/ai/**")
                .filters(f -> f.tokenRelay())
                .uri("lb://bootcamp-framework-ai"))
            // Auth server (no auth required — issues tokens)
            .route("auth", r -> r.path("/oauth2/**", "/.well-known/**")
                .uri("lb://bootcamp-framework-auth"))
            // Producer service
            .route("producer", r -> r.path("/message/**", "/user/**")
                .filters(f -> f.tokenRelay())
                .uri("lb://bootcamp-framework-producer"))
            .build();
    }
}

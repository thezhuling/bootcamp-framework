package org.github.bootcamp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * @author zhuling
 */
@SpringBootApplication
public class BootcampFrameworkGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkGatewayApplication.class, args);
  }

  @Bean
  public RouteLocator routeLocator(RouteLocatorBuilder builder) {
    return builder
        .routes()
        .route(
            p ->
                p.path("get")
                    .filters(f -> f.addRequestHeader("Hello", "World"))
                    .uri("http://localhost:8081"))
        .route(
            p ->
                p.host("*.circuitbreaker.com")
                    .filters(f -> f.circuitBreaker(config -> config.setName("mycmd")))
                    .uri("http://httpbin.org:80"))
        .build();
  }
}

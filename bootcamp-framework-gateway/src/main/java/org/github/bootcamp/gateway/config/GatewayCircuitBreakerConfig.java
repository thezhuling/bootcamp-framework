package org.github.bootcamp.gateway.config;

import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raises the time limit on the gateway's Resilience4j circuit breakers.
 *
 * <p>Resilience4j's own TimeLimiter default is one second. Any route using {@code .circuitBreaker()}
 * inherits it, so a downstream call that takes longer is cut off and the client receives the
 * {@code /fallback} 503 even though the service would have answered — with no error logged.
 *
 * <p>Only the timeout is changed: the circuit breaker settings and the rest of the time limiter
 * configuration are taken from the registries' defaults. The value is read once at startup, so a
 * change pushed from Nacos takes effect on restart.
 *
 * @author zhuling
 */
@Configuration
public class GatewayCircuitBreakerConfig {

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerTimeoutCustomizer(
            @Value("${bootcamp.gateway.circuit-breaker.timeout:5s}") Duration timeout) {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(factory.getCircuitBreakerRegistry().getDefaultConfig())
            .timeLimiterConfig(TimeLimiterConfig.from(factory.getTimeLimiterRegistry().getDefaultConfig())
                .timeoutDuration(timeout)
                .build())
            .build());
    }
}

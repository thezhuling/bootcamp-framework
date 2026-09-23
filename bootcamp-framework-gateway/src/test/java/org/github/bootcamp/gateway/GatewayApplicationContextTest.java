package org.github.bootcamp.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

/**
 * Loads the gateway context without Nacos running, and asserts the route table is the one the
 * application declares.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = BootcampFrameworkGatewayApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.gateway.discovery.locator.enabled=false",
      "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false",
      // short enough to keep the timeout tests quick, long enough to sit clear of the 1s default
      "bootcamp.gateway.circuit-breaker.timeout=2s"
    })
class GatewayApplicationContextTest {

  @MockitoBean ReactiveDiscoveryClient reactiveDiscoveryClient;

  @Autowired ApplicationContext context;

  @Autowired RouteLocator routeLocator;

  @Autowired ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

  @Value("${local.server.port}")
  int port;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }

  @Test
  void shutdownEndpointIsNotExposed() throws Exception {
    // An anonymous POST here used to stop the gateway: CSRF is off and /actuator/** was permitted.
    assertThat(context.getBeanNamesForType(ShutdownEndpoint.class)).isEmpty();
    assertThat(send("POST", "/actuator/shutdown")).isEqualTo(401);
  }

  @Test
  void onlyProbeAndScrapeActuatorEndpointsAreAnonymous() throws Exception {
    assertThat(send("GET", "/actuator/health")).isIn(200, 503);
    assertThat(send("GET", "/actuator/metrics")).isEqualTo(401);
    assertThat(send("GET", "/actuator/gateway/routes")).isEqualTo(401);
  }

  @Test
  void declaresTheFourApplicationRoutes() {
    List<String> ids = routeLocator.getRoutes().map(r -> r.getId()).collectList().block();
    assertThat(ids).contains("microservice", "ai", "auth", "producer");
  }

  @Test
  void microserviceRouteToleratesCallsSlowerThanResilience4jsOneSecondDefault() {
    // Resilience4j's own TimeLimiter default is 1s: without the gateway's customizer this
    // 1.5s downstream call is cut off and the client gets the /fallback 503 instead.
    assertThat(runThroughCircuitBreaker("microservice-cb", Duration.ofMillis(1500)))
        .isEqualTo("completed");
  }

  @Test
  void callsSlowerThanTheConfiguredTimeoutStillFallBack() {
    // The limit is raised, not removed. A distinct breaker id also shows the setting is the
    // factory default rather than something tied to the one route.
    assertThat(runThroughCircuitBreaker("timeout-probe", Duration.ofSeconds(3)))
        .isEqualTo("fallback:TimeoutException");
  }

  private int send(String method, String path) throws Exception {
    var request =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .method(method, HttpRequest.BodyPublishers.noBody())
            .build();
    return HttpClient.newHttpClient()
        .send(request, HttpResponse.BodyHandlers.discarding())
        .statusCode();
  }

  private String runThroughCircuitBreaker(String id, Duration downstreamLatency) {
    return circuitBreakerFactory
        .create(id)
        .run(
            Mono.delay(downstreamLatency).thenReturn("completed"),
            t -> Mono.just("fallback:" + t.getClass().getSimpleName()))
        .block();
  }
}

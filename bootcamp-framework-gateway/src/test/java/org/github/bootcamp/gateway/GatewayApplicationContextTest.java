package org.github.bootcamp.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Loads the gateway context without Nacos or Redis running, and asserts the route table is the one
 * the application declares.
 *
 * <p>This is the test that would have caught the Spring Cloud Gateway 5.0.3 change behind the
 * {@code @Primary} on {@code apiRateLimiter}: two {@code RateLimiter} beans and a factory that
 * autowires exactly one makes the context fail to refresh.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = BootcampFrameworkGatewayApplication.class,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.gateway.discovery.locator.enabled=false",
      "spring.cloud.gateway.server.webflux.discovery.locator.enabled=false"
    })
class GatewayApplicationContextTest {

  @MockitoBean ReactiveDiscoveryClient reactiveDiscoveryClient;

  @Autowired ApplicationContext context;

  @Autowired RouteLocator routeLocator;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }

  @Test
  void declaresTheFourApplicationRoutes() {
    List<String> ids = routeLocator.getRoutes().map(r -> r.getId()).collectList().block();
    assertThat(ids).contains("microservice", "ai", "auth", "producer");
  }

  @Test
  void bothRateLimitersRemainDistinctBeans() {
    // @Primary only picks which limiter the autoconfiguration injects. It must not drop or
    // collapse either bean — the AI route is deliberately throttled harder than the API route.
    assertThat(context.getBeanNamesForType(RedisRateLimiter.class))
        .containsExactlyInAnyOrder("apiRateLimiter", "aiRateLimiter");
    assertThat(context.getBean("apiRateLimiter", RedisRateLimiter.class))
        .isNotSameAs(context.getBean("aiRateLimiter", RedisRateLimiter.class));
  }
}

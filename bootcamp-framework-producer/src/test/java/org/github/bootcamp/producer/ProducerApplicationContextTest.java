package org.github.bootcamp.producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Loads the producer application context without Nacos or a RocketMQ broker running.
 *
 * <p>RocketMQ autoconfiguration is excluded because the producer connects to the broker eagerly at
 * startup; a mock {@link RocketMQTemplate} stands in for the beans that depend on it.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = {
      BootcampFrameworkProducerApplication.class,
      ProducerApplicationContextTest.RocketMqTestConfiguration.class
    },
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.loadbalancer.nacos.enabled=false",
      "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
    })
class ProducerApplicationContextTest {

  @Autowired ApplicationContext context;

  @Value("${local.server.port}")
  int port;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }

  @Test
  void shutdownEndpointIsNotExposed() throws Exception {
    assertThat(context.getBeanNamesForType(ShutdownEndpoint.class)).isEmpty();
    assertThat(send("POST", "/actuator/shutdown")).isEqualTo(401);
  }

  @Test
  void onlyProbeAndScrapeActuatorEndpointsAreAnonymous() throws Exception {
    assertThat(send("GET", "/actuator/health")).isIn(200, 503);
    assertThat(send("GET", "/actuator/metrics")).isEqualTo(401);
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

  @TestConfiguration
  static class RocketMqTestConfiguration {
    @Bean
    RocketMQTemplate rocketMQTemplate() {
      return Mockito.mock(RocketMQTemplate.class);
    }
  }
}

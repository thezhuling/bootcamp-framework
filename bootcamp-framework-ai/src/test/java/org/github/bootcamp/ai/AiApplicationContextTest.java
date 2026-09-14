package org.github.bootcamp.ai;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Loads the AI application context without Nacos, Redis Stack or a real OpenAI key.
 *
 * <p>This module silently could not start for some time — Spring AI 1.0.0 referenced a Boot 3.x
 * autoconfiguration class that Boot 4 does not ship. This test is what makes that class of failure
 * visible at build time.
 *
 * <p>The Redis vector store autoconfiguration is excluded because it opens a Jedis connection while
 * wiring; a mock {@link VectorStore} takes its place so the rest of the graph is still exercised.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = BootcampFrameworkAiApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.autoconfigure.exclude="
          + "org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration",
      "spring.ai.openai.api-key=test-key"
    })
class AiApplicationContextTest {

  @MockitoBean VectorStore vectorStore;

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
    // Redis is not running here, so health may legitimately report DOWN (503)
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
}

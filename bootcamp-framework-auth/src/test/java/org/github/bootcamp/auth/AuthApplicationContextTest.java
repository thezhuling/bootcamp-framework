package org.github.bootcamp.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.context.ShutdownEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;

/**
 * Loads the OAuth2 authorization server without Nacos or Redis running, and calls its protocol
 * endpoints over real HTTP.
 *
 * <p>Refreshing the context alone is not enough here. The authorization server beans all load
 * fine even when no filter chain serves {@code /oauth2/**}, which is how every protocol endpoint
 * ended up redirecting to {@code /login} without any test noticing.
 *
 * <p>Note this generates the in-memory RSA key pair on every run, which is most of the few seconds
 * the test takes.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = BootcampFrameworkAuthApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false"
    })
class AuthApplicationContextTest {

  private final HttpClient http = HttpClient.newHttpClient();

  @Autowired ApplicationContext context;

  @Value("${local.server.port}")
  int port;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }

  @Test
  void publishesTheJwkSetAnonymously() throws Exception {
    // resource servers fetch this to verify every token; a redirect here breaks all of them
    var response = get("/oauth2/jwks");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"keys\"");
  }

  @Test
  void publishesOidcDiscoveryAnonymously() throws Exception {
    var response = get("/.well-known/openid-configuration");
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"issuer\":\"http://localhost:9000\"");
  }

  @Test
  void issuesAnAccessTokenForClientCredentials() throws Exception {
    var basic =
        Base64.getEncoder()
            .encodeToString("bootcamp-client:bootcamp-secret".getBytes(StandardCharsets.UTF_8));
    var request =
        HttpRequest.newBuilder(uri("/oauth2/token"))
            .header("Authorization", "Basic " + basic)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(
                HttpRequest.BodyPublishers.ofString(
                    "grant_type=client_credentials&scope=bootcamp.read"))
            .build();
    var response = http.send(request, HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("\"access_token\"");
  }

  @Test
  void shutdownEndpointIsNotExposed() throws Exception {
    assertThat(context.getBeanNamesForType(ShutdownEndpoint.class)).isEmpty();
    var response =
        http.send(
            HttpRequest.newBuilder(uri("/actuator/shutdown"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(response.statusCode()).isNotEqualTo(200);
  }

  @Test
  void onlyProbeAndScrapeActuatorEndpointsAreAnonymous() throws Exception {
    // Redis is not running here, so health may legitimately report DOWN (503)
    assertThat(get("/actuator/health").statusCode()).isIn(200, 503);
    assertThat(get("/actuator/metrics").statusCode()).isNotEqualTo(200);
  }

  private HttpResponse<String> get(String path) throws Exception {
    return http.send(
        HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
  }

  private URI uri(String path) {
    return URI.create("http://localhost:" + port + path);
  }
}

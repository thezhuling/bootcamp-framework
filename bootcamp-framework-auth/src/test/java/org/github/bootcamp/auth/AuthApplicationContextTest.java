package org.github.bootcamp.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Loads the OAuth2 authorization server context without Nacos or Redis running.
 *
 * <p>Note this generates the in-memory RSA key pair on every run, which is most of the few seconds
 * the test takes.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = BootcampFrameworkAuthApplication.class,
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false"
    })
class AuthApplicationContextTest {

  @Autowired ApplicationContext context;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }
}

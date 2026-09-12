package org.github.bootcamp.microservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Loads the microservice application context without Nacos, RocketMQ or Redis running.
 *
 * <p>This is the guard that catches framework-upgrade breakage: a missing autoconfiguration class
 * or an ambiguous bean shows up here as a failing build rather than as a service that will not boot
 * in the environment.
 *
 * @author zhuling
 */
@SpringBootTest(
    classes = {
      BootcampFrameworkMicroServiceApplication.class,
      MicroserviceApplicationContextTest.RocketMqTestConfiguration.class
    },
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.loadbalancer.nacos.enabled=false",
      "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration",
      "microservice.ttl=60",
      "microservice.app-key=test-app",
      "microservice.secret=test-secret"
    })
class MicroserviceApplicationContextTest {

  @Autowired ApplicationContext context;

  @Test
  void contextLoads() {
    assertThat(context).isNotNull();
    assertThat(context.getBeanDefinitionCount()).isPositive();
  }

  @TestConfiguration
  static class RocketMqTestConfiguration {
    @Bean
    RocketMQTemplate rocketMQTemplate() {
      return Mockito.mock(RocketMQTemplate.class);
    }
  }
}

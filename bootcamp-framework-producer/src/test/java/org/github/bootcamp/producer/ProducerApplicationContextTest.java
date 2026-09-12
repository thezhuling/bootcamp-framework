package org.github.bootcamp.producer;

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
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.loadbalancer.nacos.enabled=false",
      "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration"
    })
class ProducerApplicationContextTest {

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

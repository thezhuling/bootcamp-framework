package org.github.bootcamp.microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit Jackson configuration to ensure ObjectMapper bean is available before
 * rocketmq-spring-boot-starter auto-configuration runs.
 *
 * @author zhuling
 */
@Configuration
public class JacksonConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}

package org.github.bootcamp.microservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit Jackson configuration to ensure ObjectMapper bean is available before
 * rocketmq-spring-boot-starter auto-configuration runs.
 *
 * <p>Modules are discovered from the classpath so that {@code jackson-datatype-jsr310} is
 * registered: {@link org.github.bootcamp.dto.MessageEvent} carries an {@code Instant}, and a bare
 * mapper rejects it ("Java 8 date/time type not supported by default"), which turned every consumed
 * event into a parse failure in {@code BootcampFrameworkConsumer}.
 *
 * @author zhuling
 */
@Configuration
public class JacksonConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().findAndRegisterModules();
  }
}

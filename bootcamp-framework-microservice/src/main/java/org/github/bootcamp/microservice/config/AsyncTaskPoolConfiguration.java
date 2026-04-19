package org.github.bootcamp.microservice.config;

import java.util.concurrent.Executor;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async executor delegating to Spring Boot's virtual-thread executor. Virtual threads are enabled
 * via spring.threads.virtual.enabled=true in application.yml.
 *
 * @author zhuling
 */
@Configuration
@EnableAsync
public class AsyncTaskPoolConfiguration {

  @Bean
  public Executor taskExecutor(SimpleAsyncTaskExecutorBuilder builder) {
    return builder.build();
  }
}

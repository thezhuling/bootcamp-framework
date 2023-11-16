package org.github.bootcamp.microservice.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author zhuling
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncTaskPoolConfiguration {
  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(2);
    executor.setQueueCapacity(2);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("async-executor-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
    executor.setRejectedExecutionHandler(
        (runnable, poolExecutor) -> {
          try {
            log.error(
                "thread pool was fulled execute rejection policy,executor-name:{},pool-size:{},core-pool-size:{},queue-size:{},task-count:{}",
                executor.getThreadNamePrefix(),
                poolExecutor.getPoolSize(),
                poolExecutor.getCorePoolSize(),
                poolExecutor.getQueue().size(),
                poolExecutor.getTaskCount());
          } catch (Exception e) {
            log.error(
                "executor-name:{},error:{}",
                executor.getThreadNamePrefix(),
                ExceptionUtils.getStackTrace(e));
          }
        });
    return executor;
  }
}

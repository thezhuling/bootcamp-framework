package org.github.bootcamp.microservice;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@ServletComponentScan
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkMicroServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkMicroServiceApplication.class, args);
  }

  @Component
  @Slf4j
  public static class AsyncTasks {

    public static Random random = new Random();

    @Async("taskExecutor1")
    public CompletableFuture<String> doTaskOne(String taskNo) throws Exception {
      log.info("开始任务：{}", taskNo);
      long start = System.currentTimeMillis();
      //      Thread.sleep(random.nextInt(10000));
      long end = System.currentTimeMillis();
      log.info("完成任务：{}，耗时：{} 毫秒", taskNo, end - start);
      return CompletableFuture.completedFuture("任务完成");
    }
  }

  @EnableAsync
  @Configuration
  @Slf4j
  static class TaskPoolConfig {
    @Bean
    public Executor taskExecutor1() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(2);
      executor.setMaxPoolSize(2);
      executor.setQueueCapacity(2);
      executor.setKeepAliveSeconds(60);
      executor.setThreadNamePrefix("executor-1-");
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
}

package org.github.bootcamp.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.github.bootcamp.microservice.config.TrendingNewsProperties;

/**
 * @author zhuling
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TrendingNewsProperties.class)
public class BootcampFrameworkMicroServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkMicroServiceApplication.class, args);
  }
}

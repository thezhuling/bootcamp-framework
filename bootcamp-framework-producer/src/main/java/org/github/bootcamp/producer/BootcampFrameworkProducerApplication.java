package org.github.bootcamp.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zhuling
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkProducerApplication {
  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkProducerApplication.class, args);
  }
}

package org.github.bootcamp.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zhuling
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkMicroServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkMicroServiceApplication.class, args);
  }
}

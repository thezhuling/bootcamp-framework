package org.github.bootcamp.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhuling
 */
@SpringBootApplication
public class BootcampFrameworkMicroServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(BootcampFrameworkMicroServiceApplication.class, args);
  }

  @GetMapping("get")
  public ResponseEntity<String> success() {
    return ResponseEntity.ok("success");
  }
}

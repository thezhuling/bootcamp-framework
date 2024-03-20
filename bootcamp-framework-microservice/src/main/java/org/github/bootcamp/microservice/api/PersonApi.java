package org.github.bootcamp.microservice.api;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("/api/v1/person")
public class PersonApi {
  @Resource private StringRedisTemplate stringRedisTemplate;

  @GetMapping("test")
  public ResponseEntity<String> test() {
    stringRedisTemplate.opsForValue().set("test", "test");
    return ResponseEntity.ok("success");
  }
}

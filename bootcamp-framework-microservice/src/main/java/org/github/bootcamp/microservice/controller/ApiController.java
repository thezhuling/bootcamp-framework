package org.github.bootcamp.microservice.controller;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.github.bootcamp.microservice.NacosConfiguration;
import org.github.bootcamp.microservice.feign.ProducerCaller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("api")
@Slf4j
public class ApiController {
  @Resource private NacosConfiguration nacosConfiguration;

  @Resource private ProducerCaller producerCaller;

  @GetMapping("user-list")
  public ResponseEntity<?> userList() {
    UserListRequestDto userListRequestDto =
        UserListRequestDto.builder()
            .traceId(UUID.randomUUID().toString())
            .token(UUID.randomUUID().toString())
            .userNos(List.of("T20230525"))
            .build();
    ResponseEntity<List<UserDto>> response = producerCaller.list(userListRequestDto);
    log.info("user-list response:{}", response);
    return ResponseEntity.ok(response.getBody());
  }

  @GetMapping("config-message")
  public ResponseEntity<String> configMessage() {
    log.info(
        "ttl:{},app-key:{},secret:{}",
        nacosConfiguration.getTtl(),
        nacosConfiguration.getAppKey(),
        nacosConfiguration.getSecret());
    return ResponseEntity.ok("success");
  }

  @PostMapping("form-data")
  public ResponseEntity<String> formData(UserDto userDto) {
    return ResponseEntity.ok("success");
  }

  @GetMapping("path/{name}/{age}")
  public ResponseEntity<String> path(
      @PathVariable("name") String name, @PathVariable("age") String age) {
    System.out.println(name);
    System.out.println(age);
    return ResponseEntity.ok("success");
  }

  @GetMapping("get")
  public ResponseEntity<String> get() {
    return ResponseEntity.ok("success");
  }
}

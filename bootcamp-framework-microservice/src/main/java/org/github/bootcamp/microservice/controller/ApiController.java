package org.github.bootcamp.microservice.controller;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.github.bootcamp.microservice.BootcampFrameworkMicroServiceApplication;
import org.github.bootcamp.microservice.NacosConfiguration;
import org.github.bootcamp.microservice.feign.ProducerCaller;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  @Resource private BootcampFrameworkMicroServiceApplication.AsyncTasks asyncTasks;

  @PostMapping(
      value = "response/type",
      consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public ResponseEntity<String> test(@RequestBody UserDto userDto) {
    log.info("param:{}", userDto);
    return ResponseEntity.ok("success");
  }

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

  @PostMapping("json-data")
  public ResponseEntity<String> jsonData(UserDto userDto) {
    log.info("requestPayload:{}", userDto);
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

  @RequestMapping("content-type")
  public ResponseEntity<String> contentType(@RequestBody UserDto userDto) {
    log.info("requestPayload:{}", userDto);
    return ResponseEntity.ok("success");
  }

  @PostMapping("text/plain")
  public ResponseEntity<String> textPlain(@RequestBody String text) {
    log.info("requestPayload:{}", text);
    return ResponseEntity.ok("success");
  }

  @GetMapping("task-test")
  public ResponseEntity<String> taskTest() {
    long start = System.currentTimeMillis();

    try {
      // 线程池1
      CompletableFuture<String> task1 = asyncTasks.doTaskOne("1");
      CompletableFuture<String> task2 = asyncTasks.doTaskOne("2");
      CompletableFuture<String> task3 = asyncTasks.doTaskOne("3");
      CompletableFuture<String> task4 = asyncTasks.doTaskOne("4");
      CompletableFuture<String> task5 = asyncTasks.doTaskOne("5");

      // 一起执行
      CompletableFuture.allOf(task1, task2, task3, task4, task5);

      long end = System.currentTimeMillis();

      log.info("任务全部完成，总耗时：" + (end - start) + "毫秒");
    } catch (Exception e) {
      log.error("execute error:{}", ExceptionUtils.getStackTrace(e));
    }
    return ResponseEntity.ok("success");
  }
}

package org.github.bootcamp.microservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("http")
@Slf4j
public class HttpRequestController {
  @PostMapping("test")
  public ResponseEntity<String> requestBody(@RequestBody UserDto userDto) {
    log.info("Get requestPayload:{}", userDto);
    return ResponseEntity.ok("success");
  }
}

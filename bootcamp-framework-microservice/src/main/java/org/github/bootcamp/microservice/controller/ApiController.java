package org.github.bootcamp.microservice.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.microservice.NacosConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

  @GetMapping("config-message")
  public ResponseEntity<String> configMessage() {
    log.info(
        "ttl:{},app-key:{},secret:{}",
        nacosConfiguration.getTtl(),
        nacosConfiguration.getAppKey(),
        nacosConfiguration.getSecret());
    return ResponseEntity.ok("success");
  }
}

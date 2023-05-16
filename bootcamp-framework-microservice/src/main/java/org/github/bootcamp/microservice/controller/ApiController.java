package org.github.bootcamp.microservice.controller;

import jakarta.annotation.Resource;
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
public class ApiController {
    @Resource
    private NacosConfiguration nacosConfiguration;

    @GetMapping("config-message")
    public ResponseEntity<String> configMessage() {
        return ResponseEntity.ok(nacosConfiguration.getTtl());
    }
}

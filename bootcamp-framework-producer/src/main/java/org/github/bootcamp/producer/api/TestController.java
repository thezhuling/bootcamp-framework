package org.github.bootcamp.producer.api;

import org.github.bootcamp.producer.service.HandleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("test")
public class TestController {
    @Autowired
    private HandleService handleService;
    @GetMapping("abstractWrapper")
    public ResponseEntity<String> abstractWrapper() {
        handleService.handle();
        return ResponseEntity.ok("success");
    }
}

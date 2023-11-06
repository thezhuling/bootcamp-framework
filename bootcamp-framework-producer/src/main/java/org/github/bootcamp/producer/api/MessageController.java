package org.github.bootcamp.producer.api;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.producer.message.MessageQueueProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("message")
@Slf4j
public class MessageController {
  @Resource private MessageQueueProducer messageQueueProducer;

  @GetMapping("send")
  private ResponseEntity<String> sendMessage() {
    messageQueueProducer.sendMessage();
    return ResponseEntity.ok("success");
  }
}

package org.github.bootcamp.microservice.api;

import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController("message")
public class MessageApi {
  @Resource private RocketMQTemplate rocketMQTemplate;

  @PostMapping("send")
  public ResponseEntity<String> send(String message) {
    Message<String> messagePayload = MessageBuilder.withPayload(message).build();
    rocketMQTemplate.send("bootcamp-framework-topic", messagePayload);
    return ResponseEntity.ok("success");
  }
}

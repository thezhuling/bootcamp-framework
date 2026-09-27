package org.github.bootcamp.microservice.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.time.Instant;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.github.bootcamp.dto.MessageEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publishes to {@value #TOPIC} in the shape {@code BootcampFrameworkConsumer} parses back: a JSON
 * {@link MessageEvent}, serialised with the same {@link ObjectMapper} bean the consumer uses so
 * the {@code Instant} round-trips.
 *
 * @author zhuling
 */
@RestController("message")
public class MessageApi {
  static final String TOPIC = "bootcamp-framework-topic";

  @Resource private RocketMQTemplate rocketMQTemplate;
  @Resource private ObjectMapper objectMapper;

  @PostMapping("send")
  public ResponseEntity<String> send(@RequestParam("message") String message)
      throws JsonProcessingException {
    var event = new MessageEvent(TOPIC, message, Instant.now());
    Message<String> messagePayload =
        MessageBuilder.withPayload(objectMapper.writeValueAsString(event)).build();
    rocketMQTemplate.send(TOPIC, messagePayload);
    return ResponseEntity.ok("success");
  }
}

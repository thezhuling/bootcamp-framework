package org.github.bootcamp.microservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.github.bootcamp.dto.MessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RocketMQ consumer using Java record pattern matching to deconstruct MessageEvent payloads.
 *
 * @author zhuling
 */
/*@Slf4j
@Component
@RocketMQMessageListener(
    topic = "bootcamp-framework-topic",
    consumerGroup = "bootcamp-framework-consumer")
public class BootcampFrameworkConsumer implements RocketMQListener<String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        try {
            var event = objectMapper.readValue(message, MessageEvent.class);
            // Java 21+ record pattern matching with when guard in switch
            switch (event) {
                case MessageEvent(var topic, var body, var timestamp) when topic != null ->
                    log.info("Received [{}] at {}: {}", topic, timestamp, body);
                default ->
                    log.warn("Received message with null topic, raw: {}", message);
            }
        } catch (Exception e) {
            log.warn("Failed to parse message as MessageEvent, raw: {}", message, e);
        }
    }
}*/

package org.github.bootcamp.microservice.consumer;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
@RocketMQMessageListener(
    topic = "bootcamp-framework-topic",
    consumerGroup = "bootcamp-framework-consumer")
public class BootcampFrameworkConsumer implements RocketMQListener<String> {

  @Override
  public void onMessage(String message) {
    System.out.println("received message: " + message);
  }
}

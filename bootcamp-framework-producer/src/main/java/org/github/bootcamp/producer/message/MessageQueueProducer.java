package org.github.bootcamp.producer.message;

import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
public class MessageQueueProducer {

  @Value("${rocketmq.producer.customized-trace-topic}")
  private String topic;

  @Resource private RocketMQTemplate rocketMQTemplate;

  public void sendMessage() {
    Message<String> message = MessageBuilder.withPayload("rocket-mq").build();
    rocketMQTemplate.send(topic, message);
  }
}

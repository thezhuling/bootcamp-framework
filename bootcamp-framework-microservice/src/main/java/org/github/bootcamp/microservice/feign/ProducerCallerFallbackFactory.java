package org.github.bootcamp.microservice.feign;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
@Slf4j
public class ProducerCallerFallbackFactory implements FallbackFactory<ProducerCallerFallback> {
  @Resource private ProducerCallerFallback producerCallerFallback;

  @Override
  public ProducerCallerFallback create(Throwable cause) {
    log.error("producer-caller was fail callback");
    return producerCallerFallback;
  }
}

package org.github.bootcamp.microservice.feign;

import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author zhuling
 */
@Slf4j
public class ProducerCallerConfiguration {
  @Qualifier("producerCallerConfiguration")
  Retryer getRetryBean() {
    return new Retryer.Default(SECONDS.toMillis(1), SECONDS.toMillis(1), 5);
  }
}

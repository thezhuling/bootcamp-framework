package org.github.bootcamp.microservice.feign;

import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Request;
import feign.Retryer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhuling
 */
@Configuration
@EnableFeignClients
public class FeignConfiguration {
  @Bean
  public Retryer feignRetryStrategy() {
    return new Retryer.Default(SECONDS.toMillis(1), SECONDS.toMillis(1), 5);
  }

  @Bean
  public Request.Options options() {
    return new Request.Options(60000, 60000);
  }

  //  @Qualifier("producerCallerConfiguration")
  //  Retryer getRetryBean() {
  //    return new Retryer.Default(SECONDS.toMillis(1), SECONDS.toMillis(1), 5);
  //  }
}

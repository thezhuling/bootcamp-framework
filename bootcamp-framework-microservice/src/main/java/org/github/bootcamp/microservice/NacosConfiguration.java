package org.github.bootcamp.microservice;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
@RefreshScope
@Data
public class NacosConfiguration {
  @Value("${microservice.ttl}")
  private String ttl;

  @Value("${microservice.app-key}")
  private String appKey;

  @Value("${microservice.secret}")
  private String secret;
}

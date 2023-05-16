package org.github.bootcamp.microservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */

@Component
@RefreshScope
public class NacosConfiguration {
    @Value("${microservice.ttl}")
    private String ttl;

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }
}

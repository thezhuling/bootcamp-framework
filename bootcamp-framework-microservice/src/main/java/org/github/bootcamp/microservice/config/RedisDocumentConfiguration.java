package org.github.bootcamp.microservice.config;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhuling
 */
@Configuration
@EnableRedisDocumentRepositories(basePackages = "org.github.bootcamp.microservice.redisom.*")
public class RedisDocumentConfiguration {}

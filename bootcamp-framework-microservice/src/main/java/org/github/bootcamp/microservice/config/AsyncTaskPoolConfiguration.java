package org.github.bootcamp.microservice.config;

import java.util.concurrent.Executor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async executor delegating to Spring Boot's virtual-thread executor.
 * Virtual threads are enabled via spring.threads.virtual.enabled=true in application.yml,
 * so we simply expose the auto-configured applicationTaskExecutor bean.
 *
 * @author zhuling
 */
@Configuration
@EnableAsync
public class AsyncTaskPoolConfiguration {

    @Bean
    public Executor taskExecutor(ApplicationContext ctx) {
        return (Executor) ctx.getBean("applicationTaskExecutor");
    }
}

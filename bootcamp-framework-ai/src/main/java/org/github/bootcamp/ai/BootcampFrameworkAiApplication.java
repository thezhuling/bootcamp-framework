package org.github.bootcamp.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring AI service: Chat (sync + streaming), RAG (vector search), Embedding.
 * Reuses the existing Redis Stack as the vector store.
 *
 * @author zhuling
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BootcampFrameworkAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootcampFrameworkAiApplication.class, args);
    }
}

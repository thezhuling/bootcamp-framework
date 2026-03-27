package org.github.bootcamp.ai.service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.ai.model.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * @author zhuling
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public AiServiceImpl(ChatClient.Builder chatClientBuilder,
                         EmbeddingModel embeddingModel,
                         VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    @Override
    public ChatResponse chat(String message, String sessionId) {
        var response = chatClient.prompt()
            .user(message)
            .call()
            .chatResponse();
        var result = response.getResult();
        var usage = response.getMetadata().getUsage();
        return new ChatResponse(
            result.getOutput().getText(),
            response.getMetadata().getModel(),
            usage != null ? usage.getTotalTokens() : 0L);
    }

    @Override
    public Flux<String> chatStream(String message) {
        return chatClient.prompt()
            .user(message)
            .stream()
            .content();
    }

    @Override
    public String rag(String question, int topK) {
        var docs = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .topK(topK > 0 ? topK : 3)
                .build());
        var context = docs.stream()
            .map(doc -> doc.getText())
            .reduce("", (a, b) -> a + "\n" + b);
        var prompt = String.format(
            "Based on the following context, answer the question.\n\nContext:\n%s\n\nQuestion: %s",
            context, question);
        return chatClient.prompt()
            .user(prompt)
            .call()
            .content();
    }

    @Override
    public List<Float> embed(String text) {
        var embedding = embeddingModel.embed(text);
        var result = new java.util.ArrayList<Float>(embedding.length);
        for (float v : embedding) {
            result.add(v);
        }
        return result;
    }
}

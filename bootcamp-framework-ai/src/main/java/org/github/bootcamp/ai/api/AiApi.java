package org.github.bootcamp.ai.api;

import java.util.List;
import org.github.bootcamp.ai.model.ChatResponse;
import org.github.bootcamp.ai.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI REST endpoints — Chat (sync + SSE streaming), RAG, Embedding.
 * Request/response types use Java records for concise definition.
 *
 * @author zhuling
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiApi {

    // Java records as request types (inline — they are small and API-specific)
    record ChatRequest(String message, String sessionId) {}
    record RagRequest(String question, int topK) {}
    record EmbedRequest(String text) {}

    private final AiService aiService;

    public AiApi(AiService aiService) {
        this.aiService = aiService;
    }

    /** Synchronous chat completion. */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return aiService.chat(request.message(), request.sessionId());
    }

    /** Streaming chat via Server-Sent Events. */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        return aiService.chatStream(request.message());
    }

    /** RAG: retrieve relevant documents then generate a response. */
    @PostMapping("/rag")
    public String rag(@RequestBody RagRequest request) {
        return aiService.rag(request.question(), request.topK());
    }

    /** Text embedding — returns the embedding vector. */
    @PostMapping("/embed")
    public List<Float> embed(@RequestBody EmbedRequest request) {
        return aiService.embed(request.text());
    }
}

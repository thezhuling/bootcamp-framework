package org.github.bootcamp.ai.service;

import java.util.List;
import org.github.bootcamp.ai.model.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * AI service contract.
 *
 * @author zhuling
 */
public interface AiService {

    ChatResponse chat(String message, String sessionId);

    Flux<String> chatStream(String message);

    String rag(String question, int topK);

    List<Float> embed(String text);
}

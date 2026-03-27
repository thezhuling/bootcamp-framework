package org.github.bootcamp.ai.model;

/**
 * Chat completion response record.
 *
 * @author zhuling
 */
public record ChatResponse(String reply, String model, long tokens) {}

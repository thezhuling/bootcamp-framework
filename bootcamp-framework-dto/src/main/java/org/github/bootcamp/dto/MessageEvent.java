package org.github.bootcamp.dto;

import java.time.Instant;

/**
 * RocketMQ message payload record.
 * Used with record pattern matching in consumers:
 * <pre>{@code
 * switch (event) {
 *     case MessageEvent(var topic, var body, var timestamp) when topic != null ->
 *         log.info("Received [{}] at {}: {}", topic, timestamp, body);
 * }
 * }</pre>
 *
 * @author zhuling
 */
public record MessageEvent(String topic, String body, Instant timestamp) {}

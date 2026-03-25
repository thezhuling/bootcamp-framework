package org.github.bootcamp.microservice.model;

import java.time.Instant;
import java.util.List;

/**
 * @author zhuling
 */
public record TrendingNewsStory(
    String title, String link, Instant publishedAt, int mentionCount, List<String> sources) {}

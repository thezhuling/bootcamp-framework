package org.github.bootcamp.microservice.model;

import java.time.Instant;
import java.util.List;

/**
 * @author zhuling
 */
public record TrendingNewsResponse(Instant refreshedAt, List<TrendingNewsStory> stories) {}

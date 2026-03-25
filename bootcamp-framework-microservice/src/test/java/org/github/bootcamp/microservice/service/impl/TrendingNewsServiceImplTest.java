package org.github.bootcamp.microservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.List;
import org.github.bootcamp.microservice.config.TrendingNewsProperties;
import org.github.bootcamp.microservice.model.TrendingNewsStory;
import org.junit.jupiter.api.Test;

class TrendingNewsServiceImplTest {

  @Test
  void shouldRankDuplicateHeadlinesAheadOfSingleMentions() {
    TrendingNewsProperties properties = new TrendingNewsProperties();
    properties.setMaxStories(10);
    TrendingNewsServiceImpl service = new TrendingNewsServiceImpl();
    service.trendingNewsProperties = properties;

    List<TrendingNewsStory> stories =
        service.rankStories(
            List.of(
                new FeedStory(
                    "Leaders agree on climate package",
                    "https://example.com/a",
                    Instant.parse("2026-03-23T01:00:00Z"),
                    "source-a"),
                new FeedStory(
                    "Leaders agree on climate package",
                    "https://example.com/b",
                    Instant.parse("2026-03-23T02:00:00Z"),
                    "source-b"),
                new FeedStory(
                    "Markets open higher in Europe",
                    "https://example.com/c",
                    Instant.parse("2026-03-23T03:00:00Z"),
                    "source-c")));

    assertEquals(2, stories.getFirst().mentionCount());
    assertEquals("Leaders agree on climate package", stories.getFirst().title());
    assertFalse(stories.getFirst().sources().isEmpty());
  }
}

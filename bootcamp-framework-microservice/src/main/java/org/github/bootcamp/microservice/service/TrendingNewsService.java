package org.github.bootcamp.microservice.service;

import java.time.Instant;
import java.util.List;
import org.github.bootcamp.microservice.model.TrendingNewsStory;

/**
 * @author zhuling
 */
public interface TrendingNewsService {

  List<TrendingNewsStory> getLatestStories();

  List<TrendingNewsStory> refreshTrendingStories();

  Instant getLastUpdatedAt();
}

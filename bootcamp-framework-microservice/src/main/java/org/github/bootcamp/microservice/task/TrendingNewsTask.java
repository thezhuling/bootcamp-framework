package org.github.bootcamp.microservice.task;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.microservice.config.TrendingNewsProperties;
import org.github.bootcamp.microservice.service.TrendingNewsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Slf4j
@Component
public class TrendingNewsTask {
  @Resource private TrendingNewsService trendingNewsService;

  @Resource private TrendingNewsProperties trendingNewsProperties;

  @Scheduled(
      initialDelayString = "${bootcamp.news.trending.schedule-fixed-delay-millis:1800000}",
      fixedDelayString = "${bootcamp.news.trending.schedule-fixed-delay-millis:1800000}")
  public void refreshTrendingNews() {
    if (!trendingNewsProperties.isEnabled()) {
      return;
    }
    log.info("Starting trending news refresh task");
    trendingNewsService.refreshTrendingStories();
  }
}

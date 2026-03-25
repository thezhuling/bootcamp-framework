package org.github.bootcamp.microservice.api;

import jakarta.annotation.Resource;
import org.github.bootcamp.microservice.model.TrendingNewsResponse;
import org.github.bootcamp.microservice.service.TrendingNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("/api/v1/news")
public class TrendingNewsApi {
  @Resource private TrendingNewsService trendingNewsService;

  @GetMapping("trending")
  public ResponseEntity<TrendingNewsResponse> trending(
      @RequestParam(value = "refresh", required = false, defaultValue = "false") boolean refresh) {
    if (refresh) {
      trendingNewsService.refreshTrendingStories();
    }
    return ResponseEntity.ok(
        new TrendingNewsResponse(
            trendingNewsService.getLastUpdatedAt(), trendingNewsService.getLatestStories()));
  }

  @PostMapping("trending/refresh")
  public ResponseEntity<TrendingNewsResponse> refresh() {
    trendingNewsService.refreshTrendingStories();
    return ResponseEntity.ok(
        new TrendingNewsResponse(
            trendingNewsService.getLastUpdatedAt(), trendingNewsService.getLatestStories()));
  }
}

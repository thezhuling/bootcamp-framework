package org.github.bootcamp.microservice.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhuling
 */
@ConfigurationProperties(prefix = "bootcamp.news.trending")
public class TrendingNewsProperties {
  private boolean enabled = true;

  private int maxStories = 10;

  private long scheduleFixedDelayMillis = 1_800_000L;

  private long requestTimeoutMillis = 10_000L;

  private List<String> feedUrls =
      List.of(
          "https://news.google.com/rss/headlines/section/topic/WORLD?hl=en-US&gl=US&ceid=US:en",
          "https://feeds.bbci.co.uk/news/world/rss.xml");

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getMaxStories() {
    return maxStories;
  }

  public void setMaxStories(int maxStories) {
    this.maxStories = maxStories;
  }

  public long getScheduleFixedDelayMillis() {
    return scheduleFixedDelayMillis;
  }

  public void setScheduleFixedDelayMillis(long scheduleFixedDelayMillis) {
    this.scheduleFixedDelayMillis = scheduleFixedDelayMillis;
  }

  public long getRequestTimeoutMillis() {
    return requestTimeoutMillis;
  }

  public void setRequestTimeoutMillis(long requestTimeoutMillis) {
    this.requestTimeoutMillis = requestTimeoutMillis;
  }

  public List<String> getFeedUrls() {
    return feedUrls;
  }

  public void setFeedUrls(List<String> feedUrls) {
    this.feedUrls = feedUrls;
  }
}

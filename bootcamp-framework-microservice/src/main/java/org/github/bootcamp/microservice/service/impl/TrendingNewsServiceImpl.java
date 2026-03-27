package org.github.bootcamp.microservice.service.impl;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;
import java.util.regex.Pattern;
import java.util.stream.Gatherers;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.microservice.config.TrendingNewsProperties;
import org.github.bootcamp.microservice.model.TrendingNewsStory;
import org.github.bootcamp.microservice.service.TrendingNewsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author zhuling
 */
@Slf4j
@Service("trendingNewsService")
public class TrendingNewsServiceImpl implements TrendingNewsService {
  private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\p{L}\\p{N}]+");

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Resource TrendingNewsProperties trendingNewsProperties;

  private volatile List<TrendingNewsStory> latestStories = List.of();

  private volatile Instant lastUpdatedAt;

  @Override
  public List<TrendingNewsStory> getLatestStories() {
    return latestStories;
  }

  @Override
  public List<TrendingNewsStory> refreshTrendingStories() {
    if (!trendingNewsProperties.isEnabled()) {
      log.info("Trending news task is disabled");
      return latestStories;
    }
    if (CollectionUtils.isEmpty(trendingNewsProperties.getFeedUrls())) {
      log.warn("No feed url configured for trending news task");
      latestStories = List.of();
      lastUpdatedAt = Instant.now();
      return latestStories;
    }

    // Java 25 preview: StructuredTaskScope for parallel feed fetching
    // Total time = max(individual fetch times) instead of sum
    List<FeedStory> feedStories = fetchAllFeedsParallel(trendingNewsProperties.getFeedUrls());

    latestStories = rankStories(feedStories);
    lastUpdatedAt = Instant.now();
    log.info(
        "Trending news refresh completed, story-count:{}, feed-count:{}",
        latestStories.size(),
        trendingNewsProperties.getFeedUrls().size());
    return latestStories;
  }

  /**
   * Fetches all RSS feeds in parallel using Java 25 Structured Concurrency (JEP 505 preview).
   * JEP 505 replaced ShutdownOnFailure with StructuredTaskScope.open(Joiner) factory API.
   * Falls back to sequential on interruption or scope failure.
   */
  @SuppressWarnings("preview")
  private List<FeedStory> fetchAllFeedsParallel(List<String> feedUrls) {
    // Java 25: explicit Joiner type to satisfy open()'s two-param signature <T, R>
    StructuredTaskScope.Joiner<List<FeedStory>, ?> joiner =
        StructuredTaskScope.Joiner.allSuccessfulOrThrow();
    try (var scope = StructuredTaskScope.open(joiner)) {
      List<StructuredTaskScope.Subtask<List<FeedStory>>> tasks = feedUrls.stream()
          .map(url -> scope.fork(() -> fetchFeedStories(url)))
          .toList();
      scope.join();
      return tasks.stream()
          .flatMap(t -> t.get().stream())
          .toList();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.warn("Feed fetch interrupted, falling back to sequential");
      return fetchAllFeedsSequential(feedUrls);
    } catch (Exception e) {
      log.warn("Parallel fetch failed, falling back to sequential", e);
      return fetchAllFeedsSequential(feedUrls);
    }
  }

  private List<FeedStory> fetchAllFeedsSequential(List<String> feedUrls) {
    List<FeedStory> result = new ArrayList<>();
    for (String url : feedUrls) {
      result.addAll(fetchFeedStories(url));
    }
    return result;
  }

  @Override
  public Instant getLastUpdatedAt() {
    return lastUpdatedAt;
  }

  List<TrendingNewsStory> rankStories(List<FeedStory> feedStories) {
    Map<String, List<FeedStory>> groupedStories = new LinkedHashMap<>();
    for (FeedStory story : feedStories) {
      if (!StringUtils.hasText(story.title())) {
        continue;
      }
      String normalizedTitle = normalizeTitle(story.title());
      if (!StringUtils.hasText(normalizedTitle)) {
        continue;
      }
      groupedStories.computeIfAbsent(normalizedTitle, key -> new ArrayList<>()).add(story);
    }

    // Java 22+ stable: Stream Gatherers — window stories for batch processing
    // Use windowSliding to inspect clusters of ranked stories
    var ranked = groupedStories.values().stream()
        .map(this::toTrendingNewsStory)
        .sorted(
            Comparator.comparingInt(TrendingNewsStory::mentionCount)
                .reversed()
                .thenComparing(
                    story -> Optional.ofNullable(story.publishedAt()).orElse(Instant.EPOCH),
                    Comparator.reverseOrder())
                .thenComparing(TrendingNewsStory::title, String.CASE_INSENSITIVE_ORDER))
        .toList();

    // Demonstrate Gatherers.windowSliding for deduplication across adjacent stories
    var deduped = ranked.stream()
        .gather(Gatherers.windowSliding(2))
        .filter(window -> window.size() < 2
            || !normalizeTitle(window.get(0).title())
                .equals(normalizeTitle(window.getLast().title())))
        .map(List::getFirst)
        .distinct()
        .limit(Math.max(trendingNewsProperties.getMaxStories(), 1))
        .toList();

    return deduped.isEmpty() ? ranked.stream()
        .limit(Math.max(trendingNewsProperties.getMaxStories(), 1))
        .toList() : deduped;
  }

  String normalizeTitle(String title) {
    return NON_WORD_PATTERN
        .matcher(title.toLowerCase(Locale.ROOT).trim())
        .replaceAll(" ")
        .trim();
  }

  private TrendingNewsStory toTrendingNewsStory(List<FeedStory> stories) {
    FeedStory primaryStory =
        stories.stream()
            .filter(story -> StringUtils.hasText(story.link()))
            .max(
                Comparator.comparing(
                    FeedStory::publishedAt, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(stories.getFirst());

    List<String> sources =
        new ArrayList<>(
            new LinkedHashSet<>(
                stories.stream().map(FeedStory::source).filter(StringUtils::hasText).toList()));

    Instant publishedAt =
        stories.stream()
            .map(FeedStory::publishedAt)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

    return new TrendingNewsStory(
        primaryStory.title(), primaryStory.link(), publishedAt, stories.size(), List.copyOf(sources));
  }

  private List<FeedStory> fetchFeedStories(String feedUrl) {
    try {
      URI feedUri = URI.create(feedUrl);
      HttpRequest request =
          HttpRequest.newBuilder(feedUri)
              .GET()
              .timeout(java.time.Duration.ofMillis(trendingNewsProperties.getRequestTimeoutMillis()))
              .header("User-Agent", "bootcamp-framework-news-task")
              .build();
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() >= 400) {
        log.warn("Failed to fetch feed:{}, status:{}", feedUrl, response.statusCode());
        return List.of();
      }
      return parseStories(response.body(), feedUri);
    } catch (Exception e) {
      log.warn("Failed to fetch or parse feed:{}", feedUrl, e);
      return List.of();
    }
  }

  List<FeedStory> parseStories(String xml, URI feedUri) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setExpandEntityReferences(false);

      Document document =
          factory
              .newDocumentBuilder()
              .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      NodeList items = document.getElementsByTagName("item");
      List<FeedStory> stories = new ArrayList<>();
      for (int i = 0; i < items.getLength(); i++) {
        Node item = items.item(i);
        if (!(item instanceof Element element)) {
          continue;
        }
        String title = childText(element, "title");
        if (!StringUtils.hasText(title)) {
          continue;
        }
        String link = childText(element, "link");
        String source = firstNonBlank(childText(element, "source"), feedUri.getHost());
        Instant publishedAt = parsePublishedAt(childText(element, "pubDate"));
        stories.add(new FeedStory(title, link, publishedAt, source));
      }
      return stories;
    } catch (Exception e) {
      log.warn("Failed to parse xml feed:{}", feedUri, e);
      return List.of();
    }
  }

  private Instant parsePublishedAt(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    List<DateTimeFormatter> formatters =
        List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME);
    for (DateTimeFormatter formatter : formatters) {
      try {
        return OffsetDateTime.parse(value, formatter).toInstant();
      } catch (DateTimeParseException ignored) {
      }
    }
    try {
      return Instant.parse(value);
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  private String childText(Element element, String tagName) {
    NodeList nodeList = element.getElementsByTagName(tagName);
    if (nodeList.getLength() == 0) {
      return null;
    }
    String value = nodeList.item(0).getTextContent();
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String firstNonBlank(String firstValue, String fallbackValue) {
    if (StringUtils.hasText(firstValue)) {
      return firstValue.trim();
    }
    return StringUtils.hasText(fallbackValue) ? fallbackValue.trim() : null;
  }
}

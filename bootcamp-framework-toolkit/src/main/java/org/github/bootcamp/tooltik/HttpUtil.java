package org.github.bootcamp.tooltik;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * @author zhuling
 */
@Slf4j
public class HttpUtil {
  public static final String X_USER = "X-User";

  private static final PathMatcher URI_PATH_MATCHER = new AntPathMatcher();
  private static final List<MediaType> WRAPPER_MEDIA_TYPES = new ArrayList<>();
  private static final List<MediaType> FORM_DATA_TYPES = new ArrayList<>();
  private static final Set<String> excludeUris = new HashSet<>();

  static {
    WRAPPER_MEDIA_TYPES.add(MediaType.TEXT_PLAIN);
    WRAPPER_MEDIA_TYPES.add(MediaType.APPLICATION_JSON);
    WRAPPER_MEDIA_TYPES.add(MediaType.TEXT_XML);

    FORM_DATA_TYPES.add(MediaType.APPLICATION_FORM_URLENCODED);
    FORM_DATA_TYPES.add(MediaType.MULTIPART_FORM_DATA);
  }

  public static boolean isFileUploadRequest(MediaType mediaType, HttpServletRequest request) {
    if (mediaType != null && mediaType.equalsTypeAndSubtype(MediaType.MULTIPART_FORM_DATA)) {
      if (request instanceof MultipartHttpServletRequest multipartRequest) {
        return !multipartRequest.getFileMap().isEmpty();
      }
    }
    return false;
  }

  public static boolean isWrapperMediaType(MediaType mediaType) {
    return mediaType != null
        && WRAPPER_MEDIA_TYPES.stream()
            .anyMatch(wrapperMediaType -> wrapperMediaType.equalsTypeAndSubtype(mediaType));
  }

  public static MediaType parseMediaType(String contentType) {
    try {
      return MediaType.parseMediaType(contentType);
    } catch (Exception e) {
      log.error("HttpUtil parse mediaType exception:{}", ExceptionUtils.getStackTrace(e));
    }
    return null;
  }

  public static String inputStream2String(InputStream inputStream) {
    StringBuilder stringBuilder = new StringBuilder();
    try (inputStream;
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
      }
    } catch (Exception e) {
      log.error("HttpUtil inputStream2String exception:{}", ExceptionUtils.getStackTrace(e));
    }
    return StringUtils.isBlank(stringBuilder)
        ? stringBuilder.toString()
        : StringUtils.replaceBlank(stringBuilder.toString());
  }

  public static String getClientIp(HttpServletRequest request) {
    String unknown = "UNKNOWN";
    // 阿里的SLB会将远程IP塞进X-Forwarded-For
    String ip = request.getHeader("X-Forwarded-For");
    if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
      ip = request.getHeader("x-forwarded-for");
    }
    if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (StringUtils.isBlank(ip) || unknown.equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }

  public static boolean isFormPost(MediaType mediaType, String httpMethod) {
    return mediaType != null
        && (mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_FORM_URLENCODED)
            || mediaType.equalsTypeAndSubtype(MediaType.MULTIPART_FORM_DATA)
                && HttpMethod.POST.matches(httpMethod));
  }

  public static boolean isFormPost(String contentType, String httpMethod) {
    MediaType mediaType = parseMediaType(contentType);
    return mediaType != null
        && (mediaType.equalsTypeAndSubtype(MediaType.APPLICATION_FORM_URLENCODED)
            || mediaType.equalsTypeAndSubtype(MediaType.MULTIPART_FORM_DATA)
                && HttpMethod.POST.matches(httpMethod));
  }

  public static boolean isMatchExclude(final String uri) {
    if (CollectionUtils.isEmpty(excludeUris)) {
      return false;
    }
    for (final String excludeUri : excludeUris) {
      if (URI_PATH_MATCHER.match(excludeUri, uri)) {
        return true;
      }
    }
    return false;
  }
}

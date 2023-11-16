package org.github.bootcamp.microservice.filter;

import com.alibaba.nacos.shaded.com.google.common.base.Stopwatch;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.github.bootcamp.dto.RequestTraceLogDto;
import org.github.bootcamp.microservice.wrapper.HttpRequestWrapper;
import org.github.bootcamp.tooltik.DateUtil;
import org.github.bootcamp.tooltik.HttpUtil;
import org.github.bootcamp.tooltik.JsonUtil;
import org.github.bootcamp.tooltik.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

/**
 * @author zhuling
 */
//@Component
//@WebFilter(filterName = "HttpRequestLoggingFilter", urlPatterns = "/**")
//@Order(10)
@Slf4j
public class HttpRequestLoggingFilter extends OncePerRequestFilter
    implements ApplicationContextAware, ApplicationListener<WebServerInitializedEvent> {
  private static final List<String> DEFAULT_DOWNLOAD_CONTENT_TYPE =
      List.of(
          "application/vnd.ms-excel", // .xls
          "application/msexcel", // .xls
          "application/cvs", // .cvs
          MediaType.APPLICATION_OCTET_STREAM_VALUE, // .*（ 二进制流，不知道下载文件类型）
          "application/x-xls", // .xls
          "application/msword", // .doc
          MediaType.TEXT_PLAIN_VALUE, // .txt
          "application/x-gzip" // .gz
          );
  @Resource private RequestMappingHandlerMapping requestMappingHandlerMapping;
  private ApplicationContext applicationContext;
  private Integer serverPort = 0;

  @Override
  protected void doFilterInternal(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull FilterChain filterChain)
      throws ServletException, IOException {

    if (HttpUtil.isMatchExclude(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    Stopwatch stopwatch = Stopwatch.createStarted();

    final boolean isWrapped = isWrappedHttpServletRequest(request);

    final HttpServletRequest wrappedRequest = isWrapped ? new HttpRequestWrapper(request) : request;

    final HttpServletResponse wrappedResponse =
        !(response instanceof ContentCachingResponseWrapper)
            ? new ContentCachingResponseWrapper(response)
            : response;

    try {
      filterChain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      saveRequestTraceLog(wrappedRequest, wrappedResponse, stopwatch);
    }
  }

  private void saveRequestTraceLog(
      HttpServletRequest wrappedRequest, HttpServletResponse wrappedResponse, Stopwatch stopwatch) {
    try {
      RequestTraceLogDto requestTraceLogDto =
          buildRequestTraceLogDto(wrappedRequest, wrappedResponse, stopwatch);

      log.info("requestTraceLogDto:{}", requestTraceLogDto);

    } catch (Exception e) {
      copyResponse(wrappedResponse);
    }
  }

  private boolean isWrappedHttpServletRequest(HttpServletRequest request) {
    final boolean isAsyncRequest = isAsyncDispatch(request);
    if (isAsyncRequest) {
      return false;
    }

    if (request instanceof ContentCachingRequestWrapper) {
      return false;
    }

    MediaType mediaType = HttpUtil.parseMediaType(request.getContentType());

    if (HttpUtil.isFileUploadRequest(mediaType, request)) {
      return false;
    }

    return HttpUtil.isWrapperMediaType(mediaType);
  }

  private HandlerMethod getHandlerMethod(
      HttpServletRequest request, HandlerMapping handlerMapping) {
    try {
      HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
      if (handlerExecutionChain != null
          && handlerExecutionChain.getHandler() instanceof HandlerMethod) {
        return (HandlerMethod) handlerExecutionChain.getHandler();
      }
    } catch (Exception e) {
      log.error(
          "[HttpRequestLoggingFilter:getHandlerMethod], error: {}",
          ExceptionUtils.getStackTrace(e));
    }
    return null;
  }

  private RequestTraceLogDto buildRequestTraceLogDto(
      HttpServletRequest wrappedRequest, HttpServletResponse wrappedResponse, Stopwatch stopwatch)
      throws Exception {

    String loggerName = StringUtils.EMPTY;
    String requestMethodName = StringUtils.EMPTY;

    HandlerMethod handlerMethod = getHandlerMethod(wrappedRequest, requestMappingHandlerMapping);

    if (handlerMethod != null) {
      loggerName = handlerMethod.getBeanType().getName();
      requestMethodName = handlerMethod.getMethod().getName();
    }

    RequestTraceLogDto.RequestTraceLogDtoBuilder builder = RequestTraceLogDto.builder();
    builder
        .logger(loggerName)
        .time(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtil.NORM_DATETIME_PATTERN)))
        .hostName(InetAddress.getLocalHost().getHostName())
        .service(getCurrentServiceName())
        .serverAddress(InetAddress.getLocalHost().getHostAddress())
        .clientAddress(HttpUtil.getClientIp(wrappedRequest))
        .contextPath(wrappedRequest.getRequestURI())
        .contentType(wrappedRequest.getContentType())
        .httpMethod(wrappedRequest.getMethod())
        .methodName(requestMethodName)
        .requestPayload(buildRequestPayload(wrappedRequest))
        .responsePayload(buildResponsePayload(wrappedRequest, wrappedResponse))
        .username(wrappedRequest.getHeader(HttpUtil.X_USER))
        .elapsed(String.valueOf(stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    return builder.build();
  }

  private String getCurrentServiceName() {
    return applicationContext.getEnvironment().getProperty("spring.application.name")
        + StringUtils.UNDERLINE
        + this.serverPort;
  }

  private String buildResponsePayload(
      HttpServletRequest wrappedRequest, HttpServletResponse wrappedResponse) {

    if (isAsyncStarted(wrappedRequest) || isDownload(wrappedResponse.getContentType())) {
      copyResponse(wrappedResponse);
      return StringUtils.EMPTY;
    }

    return getResponseString(wrappedResponse);
  }

  private void copyResponse(final HttpServletResponse response) {
    final ContentCachingResponseWrapper wrapper =
        WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    if (wrapper != null) {
      try {
        wrapper.copyBodyToResponse();
      } catch (IOException e) {
        log.error(
            "HttpRequestLoggingFilter copyResponse exception:{}", ExceptionUtils.getStackTrace(e));
      }
    }
  }

  private String getResponseString(final HttpServletResponse response) {
    final ContentCachingResponseWrapper wrapper =
        WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
    if (wrapper != null) {
      try {
        final byte[] buf = wrapper.getContentAsByteArray();
        return new String(buf, wrapper.getCharacterEncoding()).replaceAll("\n|\r", "");
      } catch (UnsupportedEncodingException e) {
        return "[UNKNOWN]";
      }
    }
    return StringUtils.EMPTY;
  }

  private boolean isDownload(String contentType) {
    return StringUtils.isNotBlank(contentType)
        && DEFAULT_DOWNLOAD_CONTENT_TYPE.stream()
            .anyMatch(
                downloadContentType ->
                    StringUtils.equalsIgnoreCase(downloadContentType, contentType));
  }

  private String buildRequestPayload(HttpServletRequest httpRequestWrapper) {
    try {
      MediaType mediaType = HttpUtil.parseMediaType(httpRequestWrapper.getContentType());

      if (HttpUtil.isFileUploadRequest(mediaType, httpRequestWrapper)) {
        return StringUtils.EMPTY;
      }

      boolean isWrapperMediaType = HttpUtil.isWrapperMediaType(mediaType);

      if (isWrapperMediaType) {
        return HttpUtil.inputStream2String(httpRequestWrapper.getInputStream());
      }

      boolean isFormData = HttpUtil.isFormPost(mediaType, httpRequestWrapper.getMethod());

      if (isFormData) {
        Map<String, String> parameterMap = getParameters(httpRequestWrapper);
        return JsonUtil.toJson(parameterMap);
      }

      // TODO urlTemplate处理

    } catch (Exception e) {
      log.error(
          "HttpRequestLoggingFilter buildRequestPayload exception:{}",
          ExceptionUtils.getStackTrace(e));
    }

    return StringUtils.EMPTY;
  }

  private Map<String, String> getParameters(HttpServletRequest httpRequestWrapper) {
    return httpRequestWrapper.getParameterMap().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.join(StringUtils.COMMA, entry.getValue()),
                (o1, o2) -> o2));
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    this.serverPort = event.getWebServer().getPort();
  }
}

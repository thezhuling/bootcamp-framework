package org.github.bootcamp.microservice.request.interceptor;

import com.alibaba.nacos.shaded.com.google.common.base.Stopwatch;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.github.bootcamp.dto.RequestTraceLogDto;
import org.github.bootcamp.microservice.request.wrapper.HttpRequestWrapper;
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * @author zhuling
 */
// @Component
@Slf4j
public class HttpRequestInterceptor
    implements HandlerInterceptor,
        ApplicationContextAware,
        ApplicationListener<WebServerInitializedEvent> {

  private ApplicationContext applicationContext;

  private Integer serverPort = 0;

  @Override
  public boolean preHandle(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler)
      throws Exception {

    //    boolean isSupportHttpMethod = RequestWrapper.isSupportHttpMethod(request.getMethod());
    //
    //    if (!isSupportHttpMethod) {
    //      return HandlerInterceptor.super.preHandle(request, response, handler);
    //    }

    //    MediaType mediaType = RequestWrapper.parseRequestMediaType(request.getContentType());

    //    boolean isSupportMediaType = RequestWrapper.isSupportMediaType(mediaType);

    //    if (!isSupportMediaType) {
    //      return HandlerInterceptor.super.preHandle(request, response, handler);
    //    }

    String className = ((HandlerMethod) handler).getBean().getClass().getName();

    String methodName = ((HandlerMethod) handler).getMethod().getName();

    //    process(request, mediaType);

    /*    if (HttpMethod.GET.matches(request.getMethod())) {
      // application/json

      BufferedReader bufferedReader =
          new BufferedReader(
              new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));

      StringBuilder responseStrBuilder = new StringBuilder();
      String inputStr;
      while ((inputStr = bufferedReader.readLine()) != null) {
        responseStrBuilder.append(inputStr);
      }

      //      Map<String, String> params = JSON.parseObject(responseStrBuilder.toString(),
      // Map.class);

      // 是否为url/xxx/xxx格式的请求
      Map<String, Object> attribute =
          objet2Map(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
    }

    // 支持form-data
    //    if (HttpMethod.GET.matches(request.getMethod())) {
    Enumeration<String> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paraName = parameterNames.nextElement();
      System.out.println(paraName + ": " + request.getParameter(paraName));
    }

    if (HttpMethod.GET.matches(request.getMethod())) {
      // 判断是否为uriTemplateVariables类型
      Map<String, Object> attribute =
          objet2Map(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
      log.info("attribute:{}", attribute);
      log.info("params:{}", ObjectUtils.isEmpty(attribute.get("m")));
      if (ObjectUtils.isEmpty(attribute.get("m"))) {}
    }*/

    return HandlerInterceptor.super.preHandle(request, response, handler);
  }

  @Override
  public void postHandle(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler,
      ModelAndView modelAndView)
      throws Exception {
    HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
  }

  private RequestTraceLogDto buildRequestTraceLogDto(
      Object handler, HttpRequestWrapper httpRequestWrapper) throws UnknownHostException {
    RequestTraceLogDto.RequestTraceLogDtoBuilder builder = RequestTraceLogDto.builder();
    builder
        .logger(((HandlerMethod) handler).getBean().getClass().getName())
        .time(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateUtil.NORM_DATETIME_PATTERN)))
        .hostName(InetAddress.getLocalHost().getHostName())
        .service(
            applicationContext.getEnvironment().getProperty("spring.application.name")
                + StringUtils.UNDERLINE
                + this.serverPort)
        .serverAddress(InetAddress.getLocalHost().getHostAddress())
        .clientAddress(HttpUtil.getClientIp(httpRequestWrapper))
        .contextPath(httpRequestWrapper.getRequestURI())
        .contentType(httpRequestWrapper.getContentType())
        .httpMethod(httpRequestWrapper.getMethod())
        .methodName(((HandlerMethod) handler).getMethod().getName())
        .requestPayload(buildRequestPayload(httpRequestWrapper))
        .username(httpRequestWrapper.getHeader(HttpUtil.X_USER));
    return builder.build();
  }

  private String buildRequestPayload(HttpRequestWrapper httpRequestWrapper) {
    try {
      MediaType mediaType = HttpUtil.parseMediaType(httpRequestWrapper.getContentType());
      boolean isWrapperMediaType = HttpUtil.isWrapperMediaType(mediaType);
      if (isWrapperMediaType) {
        return HttpUtil.inputStream2String(httpRequestWrapper.getInputStream());
      }

      boolean isFormData = HttpUtil.isFormPost(mediaType, httpRequestWrapper.getMethod());

      if (isFormData) {
        Map<String, String> parameterMap = getParameters(httpRequestWrapper);
        return JsonUtil.toJson(parameterMap);
      }

      // TODO urlTemplate处理 + 文件上传

    } catch (Exception e) {
      log.error(
          "HttpRequestInterceptor buildRequestPayload exception:{}",
          ExceptionUtils.getStackTrace(e));
    }

    return StringUtils.EMPTY;
  }

  @Override
  public void afterCompletion(
      @Nonnull HttpServletRequest request,
      @Nonnull HttpServletResponse response,
      @Nonnull Object handler,
      Exception ex)
      throws Exception {

    Stopwatch stopwatch = Stopwatch.createStarted();
    HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper(request);
    RequestTraceLogDto requestTraceLogDto = buildRequestTraceLogDto(handler, httpRequestWrapper);

    requestTraceLogDto.setElapsed(String.valueOf(stopwatch.elapsed(TimeUnit.MILLISECONDS)));

    //    ContentCachingResponseWrapper responseWrapper = new
    // ContentCachingResponseWrapper(response);
    //    String responsePayload =
    // HttpUtil.inputStream2String(responseWrapper.getContentInputStream());
    ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;
    byte[] responseBody = responseWrapper.getContentAsByteArray();
    if (responseBody.length > 0) {
      String responseBodyString = new String(responseBody, StandardCharsets.UTF_8);
      responseWrapper.copyBodyToResponse(); // 将响应体重新写入response，以便后续的处理能正常工作
      log.info("Response Body: {}", responseBodyString);
    }

    HandlerInterceptor.super.afterCompletion(request, responseWrapper, handler, ex);
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

  private Map<String, String> getParameters(HttpRequestWrapper httpRequestWrapper) {
    return httpRequestWrapper.getParameterMap().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> String.join(StringUtils.COMMA, entry.getValue()),
                (o1, o2) -> o2));
  }

  private Map<String, Object> objet2Map(Object obj) throws IllegalAccessException {
    Map<String, Object> map = new HashMap<>();
    Class<?> cla = obj.getClass();
    Field[] fields = cla.getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      map.put(field.getName(), Optional.ofNullable(field.get(obj)).orElse(""));
    }
    return map;
  }
}

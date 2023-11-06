package org.github.bootcamp.microservice.request.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.github.bootcamp.microservice.request.wrapper.HttpRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
//@Component
//@WebFilter(filterName = "ContentCachingRequestFilter", urlPatterns = "/**")
//@Order(10)
public class ContentCachingRequestFilter implements Filter {
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Filter.super.init(filterConfig);
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpRequestWrapper wrappedRequest = new HttpRequestWrapper((HttpServletRequest) servletRequest);
    filterChain.doFilter(wrappedRequest, servletResponse);
  }

  @Override
  public void destroy() {
    Filter.super.destroy();
  }
}

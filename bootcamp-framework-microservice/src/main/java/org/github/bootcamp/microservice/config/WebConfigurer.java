package org.github.bootcamp.microservice.config;

import jakarta.annotation.Resource;
import org.github.bootcamp.microservice.interceptor.ServiceAccessLogHandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhuling
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {
  @Resource private ServiceAccessLogHandlerInterceptor serviceAccessLogHandlerInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    //    registry.addInterceptor(serviceAccessLogHandlerInterceptor).addPathPatterns("/**");
  }
}

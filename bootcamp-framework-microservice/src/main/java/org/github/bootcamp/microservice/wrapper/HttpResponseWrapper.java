package org.github.bootcamp.microservice.wrapper;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * @author zhuling
 */
public class HttpResponseWrapper extends HttpServletResponseWrapper {
  /**
   * Constructs a response adaptor wrapping the given response.
   *
   * @param response The response to be wrapped
   * @throws IllegalArgumentException if the response is null
   */
  public HttpResponseWrapper(HttpServletResponse response) {
    super(response);
  }
}

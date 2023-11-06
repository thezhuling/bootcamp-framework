package org.github.bootcamp.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhuling
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestTraceLogDto implements Serializable {
  @Serial private static final long serialVersionUID = 3425858036758072318L;

  private String logger;

  private String methodName;

  private String time;

  private String service;

  private String hostName;

  private String httpMethod;

  private String contentType;

  private String contextPath;

  private String serverAddress;

  private String clientAddress;

  private String requestPayload;

  private String responsePayload;

  private String username;

  private String elapsed;
}

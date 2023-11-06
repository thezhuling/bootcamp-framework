package org.github.bootcamp.dto;

import org.github.bootcamp.dto.enums.RespStatus;

/**
 * @author ZHOU Cailiang
 * @date 2021-09-03
 */
public class BaseResponse<T> {
  private Integer status;

  private String message;

  private T data;

  public BaseResponse() {
    this(RespStatus.SUCCESS.getValue(), RespStatus.SUCCESS.getDesc());
  }

  public BaseResponse(Integer status, String message) {
    this(status, message, null);
  }

  public BaseResponse(Integer status, String message, T data) {
    this.status = status;
    this.message = message;
    this.data = data;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return "BaseResponse{"
        + "status="
        + status
        + ", message='"
        + message
        + '\''
        + ", data="
        + data
        + '}';
  }
}

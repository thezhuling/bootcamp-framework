package org.github.bootcamp.dto.enums;

/**
 * @author ZHOU Cailiang
 * @date 2021-09-03
 */
public enum RespStatus {
  /** 成功 0|success */
  SUCCESS(0, "success"),

  /** 业务请求通用失败 1|failure */
  FAILURE(-1, "failure"),

  /** 请求参数校验不通过失败 */
  ARGUMENT_NOT_VALID(1000, "method argument not valid"),

  /** 消息转换失败 */
  MESSAGE_NOT_READABLE(1001, "http message not readable"),

  /** 未授权的访问 */
  UNAUTHORIZED(9000, "unauthorized"),

  /** 访问异常失败 */
  EXCEPTION(9001, "exception");

  private final Integer value;
  private final String desc;

  RespStatus(Integer value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public Integer getValue() {
    return value;
  }

  public String getDesc() {
    return desc;
  }
}

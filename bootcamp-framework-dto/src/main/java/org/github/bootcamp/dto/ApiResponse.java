package org.github.bootcamp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ApiResponse<T> {
  private Integer status;

  private String message;

  private T data;
}

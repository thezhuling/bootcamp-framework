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
public class UserDto implements Serializable {

  @Serial private static final long serialVersionUID = 4326488870433255774L;

  private String name;
  private Integer age;
  private String sex;
  private String desc;
}

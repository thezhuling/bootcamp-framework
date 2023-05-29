package org.github.bootcamp.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
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
public class UserListRequestDto implements Serializable {
  @Serial private static final long serialVersionUID = 4610638802439142634L;

  private String traceId;

  private String token;

  private List<String> userNos;
}

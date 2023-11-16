package org.github.bootcamp.microservice.redisom.dto;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

/**
 * @author zhuling
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class Company {
  @Id private String id;
  @Searchable private String name;
  @Indexed private Point location;
  @Indexed private Set<String> tags = new HashSet<>();
  @Indexed private Integer numberOfEmployees;
  @Indexed private Integer yearFounded;
  private String url;
  private boolean publiclyListed;
}

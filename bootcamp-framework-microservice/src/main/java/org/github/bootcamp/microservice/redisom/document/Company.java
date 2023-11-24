package org.github.bootcamp.microservice.redisom.document;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TextIndexed;
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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Company {
  @Id private String id;
  @TextIndexed @Searchable private String name;
  private String url;
  @Indexed private Point location;
  @Indexed private Set<String> tags = new HashSet<>();
  @Indexed private Integer numberOfEmployees;
  @Indexed private Integer yearFounded;
  private boolean publiclyListed;
}

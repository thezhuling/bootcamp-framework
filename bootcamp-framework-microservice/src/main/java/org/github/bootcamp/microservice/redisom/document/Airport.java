package org.github.bootcamp.microservice.redisom.document;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
public class Airport implements Serializable {
  @Serial private static final long serialVersionUID = -1001031920048527262L;

  @Id private String id;

  @NonNull @Searchable private String name;

  @NonNull @Indexed private String code;

  @NonNull @Indexed private String state;
}

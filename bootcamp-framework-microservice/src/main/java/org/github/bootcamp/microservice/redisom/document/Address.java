package org.github.bootcamp.microservice.redisom.document;

import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Address {
  @NonNull private String city;

  @NonNull
  @Searchable(nostem = true)
  private String street;
}

package org.github.bootcamp.microservice.redisom.document;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import java.io.Serial;
import java.io.Serializable;
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
public class Person implements Serializable {
  @Serial
  private static final long serialVersionUID = 1442422675366159735L;

  // @Document 进行注释，这使得模型可以保存为 RedisJSON 文档
  // @Indexed 建立二级索引查询字段
  // @Searchable 支持全文搜索文本字段
  // @Id 使用ULID生成主键
  // org.github.bootcamp.microservice.redisom.document.Person:01HFNA9EV0M4ZE2H883MXYVYCY
  @Id private String id;

  @Indexed private String name;

  @Indexed private Integer age;

  @Searchable private String personalStatement;

  @Indexed private Point homeLoc;

  @Indexed private String address;

  @Indexed private Set<String> skills;
}

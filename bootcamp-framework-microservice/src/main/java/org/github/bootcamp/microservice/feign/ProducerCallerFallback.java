package org.github.bootcamp.microservice.feign;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
@Slf4j
public class ProducerCallerFallback implements ProducerCaller {
  @Override
  public ResponseEntity<List<UserDto>> list(UserListRequestDto userListRequestDto) {
    log.error("producer caller fallback");
    return ResponseEntity.ok(Collections.emptyList());
  }
}

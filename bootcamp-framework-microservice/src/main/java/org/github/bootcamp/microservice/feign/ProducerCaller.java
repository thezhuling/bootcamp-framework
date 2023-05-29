package org.github.bootcamp.microservice.feign;

import java.util.List;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zhuling
 */
@FeignClient(
    name = "bootcamp-framework-producer",
    fallbackFactory = ProducerCallerFallbackFactory.class)
public interface ProducerCaller {
  @PostMapping("/user/list")
  ResponseEntity<List<UserDto>> list(@RequestBody UserListRequestDto userListRequestDto);
}

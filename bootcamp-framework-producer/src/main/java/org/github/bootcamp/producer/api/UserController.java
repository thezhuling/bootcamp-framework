package org.github.bootcamp.producer.api;

import jakarta.annotation.Resource;
import java.net.SocketException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.github.bootcamp.producer.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserController {
  @Resource private UserService userService;

  @PostMapping("/list")
  public ResponseEntity<List<UserDto>> list(@RequestBody UserListRequestDto userListRequestDto)
      throws SocketException {
    log.info("user list requestPayload:{}", userListRequestDto);
    //    Thread.sleep(1000000);
    List<UserDto> list = userService.list(userListRequestDto);
    log.info("user list responsePayload:{}", list);
    //    return ResponseEntity.ok(list);
    throw new SocketException("build io exception");
  }
}

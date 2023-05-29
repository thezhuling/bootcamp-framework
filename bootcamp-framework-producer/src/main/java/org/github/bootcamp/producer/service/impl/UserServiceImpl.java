package org.github.bootcamp.producer.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;
import org.github.bootcamp.producer.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author zhuling
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

  @Override
  public List<UserDto> list(UserListRequestDto userListRequestDto) {
    return List.of(
        UserDto.builder().name("zs").age(23).sex("male").desc("张三").build(),
        UserDto.builder().name("lisi").age(24).sex("female").desc("李四").build(),
        UserDto.builder().name("WangWu").age(25).sex("male").desc("王五").build());
  }
}

package org.github.bootcamp.producer.service;

import java.util.List;
import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.dto.UserListRequestDto;

/**
 * @author zhuling
 */
public interface UserService {
  List<UserDto> list(UserListRequestDto userListRequestDto);
}

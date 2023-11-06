package org.github.bootcamp.producer.service.impl;

import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.producer.service.MnsService;
import org.springframework.stereotype.Service;

/**
 * @author zhuling
 */
@Service
public class MnsServiceImpl implements MnsService {
  @Override
  public void sendMessage(UserDto userDto) {
    System.out.println("name" + userDto.getName());
    System.out.println(userDto.getAge());
  }
}

package org.github.bootcamp.producer.service.impl;

import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.producer.service.HandleService;
import org.github.bootcamp.producer.service.MnsService;
import org.github.bootcamp.producer.template.ServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuling
 */
@Service
public class HandleServiceImpl extends ServiceTemplate implements HandleService {



  @Override
  public void handle() {
    this.execute();
  }

  @Override
  public UserDto createUserDto(String name, Integer age) {
    UserDto userDto = new UserDto();
    userDto.setName(name);
    userDto.setAge(age);
    return userDto;
  }
}

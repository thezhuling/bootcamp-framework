package org.github.bootcamp.producer.template;

import org.github.bootcamp.dto.UserDto;
import org.github.bootcamp.producer.service.MnsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
public abstract class ServiceTemplate {
  @Autowired private MnsService mnsService;

  public void execute() {
    UserDto userDto = createUserDto("zs", 23);
    mnsService.sendMessage(userDto);
  }

  public abstract UserDto createUserDto(String name, Integer age);
}

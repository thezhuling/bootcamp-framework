package org.github.bootcamp.producer.service;

import org.github.bootcamp.dto.UserDto;

/**
 * @author zhuling
 */
public interface MnsService {
    void sendMessage(UserDto userDto);
}

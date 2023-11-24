package org.github.bootcamp.microservice.service.impl;

import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.microservice.redisom.document.Airport;
import org.github.bootcamp.microservice.redisom.repositories.AirportsRepository;
import org.github.bootcamp.microservice.service.AirportService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author zhuling
 */
@Service
@Slf4j
public class AirportServiceImpl implements AirportService {

  @Resource RedisTemplate redisTemplate;

  @Resource private AirportsRepository airportsRepository;

  @Override
  public List<String> findByExampleAndPageable(Airport airport, Pageable pageable) {

    var o = redisTemplate.opsForValue().get("key");
    Example<Airport> example = Example.of(airport);
    Pageable pageRequest = PageRequest.of(1, 20, Sort.by(Sort.Direction.DESC, "id"));
    Page<String> page = airportsRepository.getIds(pageRequest);
    var all = airportsRepository.findAll(example, pageRequest);
    all.getContent().forEach(System.out::println);
    return page.getContent();
  }

  @Override
  public List<Airport> findAll(Airport airport) {
    return airportsRepository.findAll();
  }
}

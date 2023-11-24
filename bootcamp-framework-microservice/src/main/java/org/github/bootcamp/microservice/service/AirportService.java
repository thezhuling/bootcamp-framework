package org.github.bootcamp.microservice.service;

import java.util.List;
import org.github.bootcamp.microservice.redisom.document.Airport;
import org.springframework.data.domain.Pageable;

/**
 * @author zhuling
 */
public interface AirportService {
  List<String> findByExampleAndPageable(Airport airport, Pageable pageable);

  List<Airport> findAll(Airport airport);
}

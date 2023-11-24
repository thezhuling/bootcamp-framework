package org.github.bootcamp.microservice.api;

import jakarta.annotation.Resource;
import java.util.List;
import org.github.bootcamp.microservice.redisom.document.Airport;
import org.github.bootcamp.microservice.service.AirportService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("/api/v1/airport")
public class AirportApi {

  @Resource private AirportService airportService;

  @PostMapping("findAll")
  public ResponseEntity<List<Airport>> findAll(@RequestBody Airport airport) {
    List<Airport> airports = airportService.findAll(airport);
    return ResponseEntity.ok(airports);
  }

  @PostMapping("findByExampleAndPageable")
  public ResponseEntity<List<String>> findIdByExampleAndPageable(
      @RequestBody Airport airport, @PageableDefault(page = 1, size = 20) Pageable pageable) {
    List<String> airports = airportService.findByExampleAndPageable(airport, pageable);
    return ResponseEntity.ok(airports);
  }
}

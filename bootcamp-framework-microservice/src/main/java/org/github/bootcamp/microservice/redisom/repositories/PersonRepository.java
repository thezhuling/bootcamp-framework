package org.github.bootcamp.microservice.redisom.repositories;

import com.redis.om.spring.repository.RedisDocumentRepository;
import org.github.bootcamp.microservice.redisom.document.Person;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

/**
 * @author zhuling
 */
public interface PersonRepository extends RedisDocumentRepository<Person, String> {
  // Find people by age range
  Iterable<Person> findByAgeBetween(int minAge, int maxAge);

  // Draws a circular geofilter around a spot and returns all people in that radius
  Iterable<Person> findByHomeLoc(Point point, Distance distance);

  // Find people by their first and last name
  Iterable<Person> findByName(String name);

  // Performs full text search on a person's personal Statement
  Iterable<Person> searchByPersonalStatement(String text);
}

package org.github.bootcamp.microservice.redisom.repositories;

import com.redis.om.spring.repository.RedisDocumentRepository;
import org.github.bootcamp.microservice.redisom.document.Airport;
import org.springframework.stereotype.Repository;

/**
 * @author zhuling
 */
@Repository
public interface AirportsRepository extends RedisDocumentRepository<Airport, String> {}

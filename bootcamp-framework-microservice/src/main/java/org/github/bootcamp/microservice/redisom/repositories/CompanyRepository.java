package org.github.bootcamp.microservice.redisom.repositories;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisDocumentRepository;
import java.util.Optional;
import java.util.Set;
import org.github.bootcamp.microservice.redisom.dto.Company;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.Param;

/**
 * @author zhuling
 */
public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
    // find one by property
    Optional<Company> findOneByName(String name);

    // geospatial query
    Iterable<Company> findByLocationNear(Point point, Distance distance);

    // find by tag field, using JRediSearch "native" annotation
    @Query("@tags:{$tags}")
    Iterable<Company> findByTags(@Param("tags") Set<String> tags);

    // find by numeric property
    Iterable<Company> findByNumberOfEmployees(int noe);

    // find by numeric property range
    Iterable<Company> findByNumberOfEmployeesBetween(int noeGT, int noeLT);

    // starting with/ending with
    Iterable<Company> findByNameStartingWith(String prefix);
}

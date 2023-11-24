package org.github.bootcamp.microservice.service;

import java.util.List;
import org.github.bootcamp.microservice.redisom.document.Company;
import org.springframework.data.domain.Pageable;

/**
 * @author zhuling
 */
public interface CompanyService {

  List<Company> findAll();

  Company findOneByName(String name);

  List<Company> findListByEntityStream();

  List<Company> findByEntityStreamAndNameContaining(String prefix);

  List<Company> findListByEntityStreamMetamodel();

  List<Company> findByNameStartingWith(String prefix);

  List<Company> findByNameContaining(String keyword);

  List<Company> findByExample(Company company);

  List<Company> findByExampleAndMatcher(Company company);

  List<Company> findByExampleAndPageable(Pageable pageable, Company company);
}

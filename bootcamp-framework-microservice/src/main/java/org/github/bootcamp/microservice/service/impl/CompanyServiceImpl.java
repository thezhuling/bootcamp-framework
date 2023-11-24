package org.github.bootcamp.microservice.service.impl;

import com.redis.om.spring.search.stream.EntityStream;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.github.bootcamp.microservice.redisom.document.Company;
import org.github.bootcamp.microservice.redisom.document.Company$;
import org.github.bootcamp.microservice.redisom.repositories.CompanyRepository;
import org.github.bootcamp.microservice.service.CompanyService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import redis.clients.jedis.search.aggr.SortedField;

/**
 * @author zhuling
 */
@Service("companyService")
public class CompanyServiceImpl implements CompanyService {
  @Resource private CompanyRepository companyRepository;

  @Resource private EntityStream entityStream;

  @Override
  public List<Company> findAll() {
    return companyRepository.findAll();
  }

  @Override
  public Company findOneByName(String name) {
    return companyRepository.findOneByName(name).orElseGet(Company::new);
  }

  @Override
  public List<Company> findListByEntityStream() {
    return entityStream.of(Company.class).collect(Collectors.toList());
  }

  @Override
  public List<Company> findByEntityStreamAndNameContaining(String prefix) {
    return entityStream
        .of(Company.class)
        .filter(Company$.NAME.containing(prefix))
        .collect(Collectors.toList());
  }

  public List<Company> findListByEntityStreamMetamodel() {
    return entityStream
        .of(Company.class)
        .filter(Company$.NUMBER_OF_EMPLOYEES.gt(1000))
        .filter(Company$.YEAR_FOUNDED.between(1990, 2000))
        .sorted(Company$.NUMBER_OF_EMPLOYEES, SortedField.SortOrder.DESC)
        .collect(Collectors.toList());
  }

  @Override
  public List<Company> findByNameStartingWith(String prefix) {
    return companyRepository.findByNameStartingWith(prefix);
  }

  @Override
  public List<Company> findByNameContaining(String keyword) {
    return StreamSupport.stream(
            companyRepository.findByNameContaining(keyword).spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public List<Company> findByExample(Company company) {
    Example<Company> example = Example.of(Company.builder().name(company.getName()).build());
    return StreamSupport.stream(companyRepository.findAll(example).spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public List<Company> findByExampleAndMatcher(Company company) {

    ExampleMatcher matcher =
        ExampleMatcher.matching()
            .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.startsWith())
            .withIncludeNullValues();

    Company example = Company.builder().name(company.getName()).build();

    Example<Company> companyExample = Example.of(example, matcher);

    return StreamSupport.stream(companyRepository.findAll(companyExample).spliterator(), false)
        .collect(Collectors.toList());
  }

  @Override
  public List<Company> findByExampleAndPageable(Pageable pageable, Company company) {
    List<Sort.Order> orders = new ArrayList<>();
    orders.add(new Sort.Order(Sort.Direction.DESC, "numberOfEmployees"));
    Pageable pageRequest =
        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    Example<Company> example = Example.of(company);
    Page<Company> page = companyRepository.findAll(example, pageRequest);
    return page.getContent();
  }
}

package org.github.bootcamp.microservice.api;

import jakarta.annotation.Resource;
import java.util.List;
import org.github.bootcamp.microservice.redisom.document.Company;
import org.github.bootcamp.microservice.service.CompanyService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhuling
 */
@RestController
@RequestMapping("/api/v1/company")
public class CompanyApi {

  @Resource private CompanyService companyService;

  /** SpringData Find company all */
  @GetMapping("findAll")
  public ResponseEntity<List<Company>> findAll() {
    List<Company> companies = companyService.findAll();
    return ResponseEntity.ok(companies);
  }

  /** SpringData Find company by name */
  @GetMapping("findByName/{name}")
  public ResponseEntity<Company> findByName(@PathVariable("name") String name) {
    Company company = companyService.findOneByName(name);
    return ResponseEntity.ok(company);
  }

  /** SpringData Find company by name prefix */
  @GetMapping("findByNameStartingWith/{prefix}")
  public ResponseEntity<List<Company>> findByNameStartingWith(
      @PathVariable("prefix") String prefix) {
    List<Company> companies = companyService.findByNameStartingWith(prefix);
    return ResponseEntity.ok(companies);
  }

  @GetMapping("findByEntityStreamAndNameContaining/{prefix}")
  public ResponseEntity<List<Company>> findByEntityStreamAndNameContaining(
      @PathVariable("prefix") String prefix) {
    List<Company> companies = companyService.findByEntityStreamAndNameContaining(prefix);
    return ResponseEntity.ok(companies);
  }

  @GetMapping("findByNameContaining/{keyword}")
  public ResponseEntity<List<Company>> findByNameContaining(
      @PathVariable("keyword") String keyword) {
    List<Company> companies = companyService.findByNameContaining(keyword);
    return ResponseEntity.ok(companies);
  }

  /** EntityStream find company list */
  @GetMapping("findListByEntityStream")
  public ResponseEntity<List<Company>> findListByEntityStream() {
    List<Company> companies = companyService.findListByEntityStream();
    return ResponseEntity.ok(companies);
  }

  /** EntityStream find company list greater than 1000 employees */
  @GetMapping("findListByEntityStreamMetamodel")
  public ResponseEntity<List<Company>> findListByEntityStreamMetamodel() {
    List<Company> companies = companyService.findListByEntityStreamMetamodel();
    return ResponseEntity.ok(companies);
  }

  @PostMapping("findByExample")
  public ResponseEntity<List<Company>> findByExample(@RequestBody Company company) {
    List<Company> companies = companyService.findByExample(company);
    return ResponseEntity.ok(companies);
  }

  @PostMapping("findByExampleAndMatcher")
  public ResponseEntity<List<Company>> findByExampleAndMatcher(@RequestBody Company company) {
    List<Company> companies = companyService.findByExampleAndMatcher(company);
    return ResponseEntity.ok(companies);
  }

  @PostMapping("findByExampleAndPageable")
  public ResponseEntity<List<Company>> findByExampleAndPageable(
      Pageable pageable, @RequestBody Company company) {
    List<Company> companies = companyService.findByExampleAndPageable(pageable,company);
    return ResponseEntity.ok(companies);
  }
}

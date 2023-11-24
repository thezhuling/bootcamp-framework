package org.github.bootcamp.microservice.redisom.runner;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import jakarta.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.github.bootcamp.microservice.redisom.document.Address;
import org.github.bootcamp.microservice.redisom.document.Airport;
import org.github.bootcamp.microservice.redisom.document.Attribute;
import org.github.bootcamp.microservice.redisom.document.Company;
import org.github.bootcamp.microservice.redisom.document.Order;
import org.github.bootcamp.microservice.redisom.document.Permit;
import org.github.bootcamp.microservice.redisom.document.Person;
import org.github.bootcamp.microservice.redisom.repositories.AirportsRepository;
import org.github.bootcamp.microservice.redisom.repositories.CompanyRepository;
import org.github.bootcamp.microservice.redisom.repositories.PermitRepository;
import org.github.bootcamp.microservice.redisom.repositories.PersonRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

/**
 * @author zhuling
 */
@Component
public class RedisLoadCommandLineRunner implements CommandLineRunner {
  @Resource private CompanyRepository companyRepository;

  @Resource private PersonRepository personRepository;

  @Resource private AirportsRepository airportsRepository;

  @Resource private PermitRepository permitRepository;

  @Override
  public void run(String... args) throws Exception {
    // save companies to the database
    companyRepository.deleteAll();
    companyRepository.saveAll(initializationCompany());
    // save person to the database
    personRepository.deleteAll();
    personRepository.saveAll(initializationPerson());

    // save permit to the database
    permitRepository.deleteAll();
    permitRepository.saveAll(initializationPermit());

    // save airport to the database
    //    airportsRepository.deleteAll();
    //    String path = Resources.getResource("data/airport_codes.csv").getFile();
    //    airportsRepository.saveAll(initializationAirports(new File(path)));
  }

  private List<Permit> initializationPermit() {
    // # Document 1
    Address address1 = Address.of("Lisbon", "25 de Abril");
    Order order1 = Order.of("O11", 1.5);
    Order order2 = Order.of("O12", 5.6);
    Attribute attribute11 = Attribute.of("size", "S", Lists.newArrayList(order1));
    Attribute attribute12 = Attribute.of("size", "M", Lists.newArrayList(order2));
    List<Attribute> attrList1 = Lists.newArrayList(attribute11, attribute12);
    Permit permit1 =
        Permit.of(
            address1,
            "To construct a single detached house with a front covered veranda.",
            "single detached house",
            Set.of("demolition", "reconstruction"),
            42000L, //
            new Point(38.7635877, -9.2018309),
            List.of("started", "in_progress", "approved"),
            attrList1);

    // # Document 2
    Address address2 = Address.of("Porto", "Av. da Liberdade");
    Order order21 = Order.of("O21", 1.2);
    Order order22 = Order.of("O22", 5.6);
    Attribute attribute21 = Attribute.of("color", "red", Lists.newArrayList(order21));
    Attribute attribute22 = Attribute.of("color", "blue", Lists.newArrayList(order22));
    List<Attribute> attrList2 = Lists.newArrayList(attribute21, attribute22);
    Permit permit2 =
        Permit.of(
            address2,
            "To construct a loft",
            "apartment",
            Set.of("construction"),
            53000L,
            new Point(38.7205373, -9.148091),
            List.of("started", "in_progress", "rejected"),
            attrList2);

    // # Document 3
    Address address3 = Address.of("Lagos", "D. João");
    Order order31 = Order.of("ABC", 1.6);
    Order order32 = Order.of("DEF", 1.3);
    Order order33 = Order.of("GHJ", 1.6);
    Order order34 = Order.of("VBN", 1.0);
    Attribute attribute31 = Attribute.of("brand", "A", Lists.newArrayList(order31, order33));
    Attribute attribute32 = Attribute.of("brand", "B", Lists.newArrayList(order32, order34));
    List<Attribute> attrList3 = Lists.newArrayList(attribute31, attribute32);
    Permit permit3 =
        Permit.of(
            address3,
            "New house build",
            "house",
            Set.of("construction", "design"),
            260000L,
            new Point(37.0990749, -8.6868258),
            List.of("started", "in_progress", "postponed"),
            attrList3);

    return List.of(permit1, permit2, permit3);
  }

  private List<Person> initializationPerson() {
    Person zhangSan =
        Person.builder()
            .name("zhangsan")
            .age(23)
            .personalStatement(
                "My name is zhangsan. I am 23 years old and born in Qingdao. I graduated from Hebei University of Science and Technology. My major is English. And I got my bachelor degree after my graduation. I also studied Audit in Hebei Normal University of Science and Technology. I am very interested in English and study very hard on this subject. I had passed TEM-8 and BEC Vantage. I worked in an American company at the beginning of this year. My spoken English was improved a lot by communicating with Americans frequently during that period")
            .homeLoc(new Point(-122.066540, 37.377690))
            .address("Qingdao")
            .skills(Set.of("java", "c", "redis"))
            .build();

    Person lisi =
        Person.builder()
            .name("lisi")
            .age(24)
            .personalStatement(
                "As a motto goes attitude is everything. If I get this job, I will put all my heart in it and try my best to do it well")
            .homeLoc(new Point(-112.066540, 30.377690))
            .address("BeiJin")
            .skills(Set.of("python", "c", "mysql"))
            .build();

    Person wangWu =
        Person.builder()
            .name("wangwu")
            .age(25)
            .personalStatement("Never deter till tomorrow that which you can dotoday")
            .homeLoc(new Point(-118.066540, 10.377690))
            .address("ShangHai")
            .skills(Set.of("go", "c", "redis"))
            .build();
    return List.of(zhangSan, lisi, wangWu);
  }

  private List<Company> initializationCompany() {
    Company redis =
        Company.builder()
            .name("Redis")
            .url("https://redis.com")
            .location(new Point(-122.066540, 37.377690))
            .numberOfEmployees(526)
            .yearFounded(2011)
            .tags(Set.of("fast", "scalable", "reliable"))
            .build();

    Company microsoft =
        Company.builder()
            .name("Microsoft")
            .url("https://microsoft.com")
            .location(new Point(-122.124500, 47.640160))
            .numberOfEmployees(182268)
            .yearFounded(1975)
            .tags(Set.of("innovative", "reliable"))
            .build();

    Company google =
        Company.builder()
            .name("Google")
            .url("https://google.com")
            .location(new Point(-120.124500, 40.640160))
            .numberOfEmployees(888)
            .yearFounded(1990)
            .tags(Set.of("innovative", "reliable", "unbelievable"))
            .build();

    Company gucci =
        Company.builder()
            .name("Gucci")
            .url("https://gucci.com")
            .location(new Point(-100.124500, 20.640160))
            .numberOfEmployees(1888)
            .yearFounded(2002)
            .tags(Set.of("innovative", "reliable", "unbelievable"))
            .build();

    Company facebook =
        Company.builder()
            .name("Facebook")
            .url("https://facebook.com")
            .location(new Point(-118.124500, 28.640160))
            .numberOfEmployees(1024)
            .yearFounded(1996)
            .tags(Set.of("innovative", "reliable", "unbelievable"))
            .build();

    return List.of(redis, microsoft, google, gucci, facebook);
  }

  private List<Airport> initializationAirports(File dataFile) throws Exception {
    return Files.readLines(dataFile, StandardCharsets.UTF_8).stream()
        .map(l -> l.split(","))
        .map(ar -> Airport.builder().name(ar[0]).code(ar[1]).state(ar[2]).build())
        .collect(Collectors.toList());
  }
}

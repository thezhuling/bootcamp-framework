import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.github.bootcamp.microservice.BootcampFrameworkMicroServiceApplication;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author zhuling
 */
@SpringBootTest(
    classes = {
      BootcampFrameworkMicroServiceApplication.class,
      RedisApplicationTests.RocketMqTestConfiguration.class
    },
    properties = {
      "spring.cloud.nacos.discovery.enabled=false",
      "spring.cloud.nacos.config.enabled=false",
      "spring.cloud.service-registry.auto-registration.enabled=false",
      "spring.cloud.loadbalancer.nacos.enabled=false",
      "spring.autoconfigure.exclude=org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration",
      "microservice.ttl=60",
      "microservice.app-key=test-app",
      "microservice.secret=test-secret"
    })
public class RedisApplicationTests {

  @Test
  public void test() {}

  @TestConfiguration
  static class RocketMqTestConfiguration {
    @Bean
    RocketMQTemplate rocketMQTemplate() {
      return Mockito.mock(RocketMQTemplate.class);
    }
  }
}

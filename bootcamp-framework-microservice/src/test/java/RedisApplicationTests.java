import jakarta.annotation.Resource;
import org.github.bootcamp.microservice.BootcampFrameworkMicroServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author zhuling
 */
@SpringBootTest(classes = BootcampFrameworkMicroServiceApplication.class)
public class RedisApplicationTests {
  @Resource private StringRedisTemplate stringRedisTemplate;

  @Test
  public void test() {
    stringRedisTemplate.opsForValue().set("test", "test");
    System.out.println(stringRedisTemplate.opsForValue().get("test"));
  }
}

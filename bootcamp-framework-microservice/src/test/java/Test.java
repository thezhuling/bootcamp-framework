import java.util.HashMap;
import java.util.Map;
import org.github.bootcamp.dto.UserDto;

/**
 * @author zhuling
 */
public class Test {
  public static void main(String[] args) {
    UserDto userDto = new UserDto();
    userDto.setName(null);
    Map<String, String> map = new HashMap<>();
    map.put("", "110");
    map.put(null, "110");
    System.out.println(map.size() == 1);
  }
}

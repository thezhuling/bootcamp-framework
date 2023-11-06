import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.github.bootcamp.tooltik.StringUtils;
import org.junit.jupiter.api.Test;

/**
 * @author zhuling
 */
@Slf4j
public class Chapter78ApplicationTests {

  @Test
  public void test2() throws Exception {
    List<String> arr = new ArrayList<>();
    arr.add("a");
    arr.add("b");
    arr.add("c");
    arr.add("d");

    var result = printStr(arr);
    System.out.println(result);
  }

  private String printStr(List<String> arr) {
    for (String str : arr) {
      System.out.println(str);
      if (str.equalsIgnoreCase("c")) {
        return str;
      }
    }
    return StringUtils.EMPTY;
  }
}

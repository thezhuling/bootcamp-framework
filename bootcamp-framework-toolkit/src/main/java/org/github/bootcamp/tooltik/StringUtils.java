package org.github.bootcamp.tooltik;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhuling
 */
public class StringUtils {
  public static final String LF = "\n";
  public static final String CR = "\r";
  public static final String EMPTY = "";
  public static final String COMMA = ",";
  public static final String UNDERLINE = "_";
  private static final String REPLACE_BLANK_ENTER = "\\s{2,}|\t|\r|\n";
  private static final Pattern REPLACE_P = Pattern.compile(REPLACE_BLANK_ENTER);

  public static boolean isNotBlank(final CharSequence cs) {
    return !isBlank(cs);
  }

  public static boolean isBlank(final CharSequence cs) {
    final int strLen = length(cs);
    if (strLen == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static int length(final CharSequence cs) {
    return cs == null ? 0 : cs.length();
  }

  public static boolean isEmpty(final CharSequence cs) {
    return cs == null || cs.length() == 0;
  }

  public static String replaceAllBlank(String str) {
    String dest = EMPTY;
    if (StringUtils.isNotBlank(str)) {
      Matcher m = REPLACE_P.matcher(str);
      dest = m.replaceAll("");
    }
    return dest;
  }

  /** 去除字符串中的空格、回车、换行符、制表符 \n 回车 \t 水平制表符 \s 空格 \r 换行 */
  public static String replaceBlank(String source) {
    String ret = StringUtils.EMPTY;
    if (StringUtils.isNotBlank(source)) {
      ret =
          source
              .replaceAll(StringUtils.LF, StringUtils.EMPTY)
              .replaceAll("\\s{2,}", StringUtils.EMPTY)
              .replaceAll("\\t", StringUtils.EMPTY)
              .replaceAll(StringUtils.CR, StringUtils.EMPTY);
    }
    return ret;
  }

  public static boolean equalsIgnoreCase(final CharSequence cs1, final CharSequence cs2) {
    if (cs1 == cs2) {
      return true;
    }
    if (cs1 == null || cs2 == null) {
      return false;
    }
    if (cs1.length() != cs2.length()) {
      return false;
    }
    return CharSequenceUtils.regionMatches(cs1, true, 0, cs2, 0, cs1.length());
  }
}

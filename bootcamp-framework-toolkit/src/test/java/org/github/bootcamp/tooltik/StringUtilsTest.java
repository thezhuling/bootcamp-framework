package org.github.bootcamp.tooltik;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Pins the behaviour of {@link StringUtils}, including the cases where blank and empty differ and
 * where only runs of two or more spaces are collapsed.
 *
 * @author zhuling
 */
class StringUtilsTest {

  @Test
  void blankCountsWhitespaceButEmptyDoesNot() {
    assertThat(StringUtils.isBlank(null)).isTrue();
    assertThat(StringUtils.isBlank("")).isTrue();
    assertThat(StringUtils.isBlank("   ")).isTrue();
    assertThat(StringUtils.isBlank(" a ")).isFalse();

    // a lone space is blank but not empty — the two predicates are not interchangeable
    assertThat(StringUtils.isEmpty(null)).isTrue();
    assertThat(StringUtils.isEmpty("")).isTrue();
    assertThat(StringUtils.isEmpty(" ")).isFalse();
  }

  @Test
  void isNotBlankIsTheNegationOfIsBlank() {
    assertThat(StringUtils.isNotBlank("a")).isTrue();
    assertThat(StringUtils.isNotBlank("  ")).isFalse();
    assertThat(StringUtils.isNotBlank(null)).isFalse();
  }

  @Test
  void lengthTreatsNullAsZero() {
    assertThat(StringUtils.length(null)).isZero();
    assertThat(StringUtils.length("")).isZero();
    assertThat(StringUtils.length("abc")).isEqualTo(3);
  }

  @Test
  void replaceAllBlankStripsTabsNewlinesAndRunsOfSpaces() {
    assertThat(StringUtils.replaceAllBlank("a  b\tc\nd\re")).isEqualTo("abcde");
    // single spaces survive: the pattern only matches two or more
    assertThat(StringUtils.replaceAllBlank("a b")).isEqualTo("a b");
  }

  @Test
  void replaceAllBlankReturnsEmptyForBlankInput() {
    assertThat(StringUtils.replaceAllBlank(null)).isEqualTo(StringUtils.EMPTY);
    assertThat(StringUtils.replaceAllBlank("   ")).isEqualTo(StringUtils.EMPTY);
  }

  @Test
  void replaceBlankStripsTabsNewlinesAndRunsOfSpaces() {
    assertThat(StringUtils.replaceBlank("a\nb  c\td\re")).isEqualTo("abcde");
    assertThat(StringUtils.replaceBlank(null)).isEqualTo(StringUtils.EMPTY);
  }

  @Test
  void equalsIgnoreCaseComparesCaseInsensitivelyAndNullSafely() {
    assertThat(StringUtils.equalsIgnoreCase("ABC", "abc")).isTrue();
    assertThat(StringUtils.equalsIgnoreCase(null, null)).isTrue();
    assertThat(StringUtils.equalsIgnoreCase(null, "a")).isFalse();
    assertThat(StringUtils.equalsIgnoreCase("a", null)).isFalse();
    assertThat(StringUtils.equalsIgnoreCase("ab", "abc")).isFalse();
  }
}

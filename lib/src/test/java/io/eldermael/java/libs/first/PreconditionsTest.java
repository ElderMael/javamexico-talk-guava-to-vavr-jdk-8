package io.eldermael.java.libs.first;

import com.google.common.base.Preconditions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

// Guava preconditions vs JDK similar methods
// https://github.com/google/guava/wiki/PreconditionsExplained
public class PreconditionsTest {

  // Check for nulls
  @Test
  void shouldThrowNullPointerExceptionUsingGuava() {
    assertThatExceptionOfType(NullPointerException.class)
        .as("[JDK] Assert code will throw null.")
        .isThrownBy(() -> {
          var possiblyNull = willBeNull();
          var wontBeNull = Preconditions.checkNotNull(
              possiblyNull,
              "'possiblyNull' is %s",
              possiblyNull);
        })
        .withMessage("'possiblyNull' is null");
  }

  // Let's ignore null can be handled better :)
  @Test
  void shouldThrowNullPointerExceptionUsingJdk() {
    assertThatExceptionOfType(NullPointerException.class)
        .as("[JDK] Assert code will throw null.")
        .isThrownBy(() -> {
          String possiblyNull = willBeNull();
          // Uses supplier to create a message
          var wontBeNull = Objects.requireNonNull(possiblyNull,
              () -> String.format("'possiblyNull' is %s", possiblyNull));
        })
        .withMessage("'possiblyNull' is null");
  }

  // Check for indexes out of bounds
  @Test
  void shouldCheckListUsingGuava() {
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .as("[Guava] Assert index is out of bounds")
        .isThrownBy(() -> {
          var list = List.of(1, 2);
          Preconditions.checkPositionIndex(
              3,
              list.size()
          );
        })
        .withMessage("index (3) must not be greater than size (2)");
  }

  @Test
  void shouldCheckListUsingJdkUtil() {
    assertThatExceptionOfType(IndexOutOfBoundsException.class)
        .as("[JDK] Assert index is out of bounds")
        .isThrownBy(() -> {
          var list = List.of(1, 2);
          Objects.checkIndex(
              3,
              list.size()
          );
        })
        .withMessage("Index 3 out of bounds for length 2");
  }

  // Other types of preconditions not existing in java.util.Objects
  @Test
  void shouldCheckUsingGuavaUtil() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .as("[Guava] Assert argument passed to method")
        .isThrownBy(() -> {
          var positive = willNotAcceptNegativeInteger(-1);
        })
        .withMessage("Cannot work with negative numbers, you passed: -1");
  }

  private Integer willNotAcceptNegativeInteger(Integer zeroOrPositive) {

    Preconditions.checkArgument( // This does not exists in JDK Objects
        zeroOrPositive >= 0,
        "Cannot work with negative numbers, you passed: %s",
        zeroOrPositive);

    return zeroOrPositive;
  }

  private String willBeNull() {
    return null;
  }

}

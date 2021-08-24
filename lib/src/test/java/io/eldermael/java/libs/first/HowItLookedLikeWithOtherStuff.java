package io.eldermael.java.libs.first;

import com.google.common.base.Optional;
import io.eldermael.java.libs.BaseTestConfiguration;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class HowItLookedLikeWithOtherStuff extends BaseTestConfiguration {

  @Test
  void shouldShowDifferencesBetweenOptionals() {
    // Mapping to null will throw an NPE
    // API is very reduced, recommended to use JDK/Vavr
    // Serializable
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> {
          Optional.of("").transform(s -> null);
        });

    // If the map operation returns null, Optional.empty() is returned
    // API is more extensive
    // Non-serializable
    assertThat(java.util.Optional.of("").map(s -> null))
        .isEmpty();

    // Monadic
    // API is very substantial, tons of compatibility methods with JDK
    // Serializable
    assertThat(Option.of("").map(s -> null).get())
        .isEqualTo(null);
  }

}

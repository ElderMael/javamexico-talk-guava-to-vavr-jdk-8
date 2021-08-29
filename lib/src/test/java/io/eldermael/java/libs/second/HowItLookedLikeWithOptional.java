package io.eldermael.java.libs.second;

import com.google.common.base.Optional;
import io.eldermael.java.libs.BaseTestConfiguration;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class HowItLookedLikeWithOptional extends BaseTestConfiguration {

  @Test
  void shouldShowDifferencesBetweenOptionals() {
    // Mapping to null will throw an NPE
    // API is very reduced, recommended to use JDK/Vavr
    // Serializable
    assertThatExceptionOfType(NullPointerException.class)
        .as("[Guava] Optional throws exceptions when mapping to null")
        .isThrownBy(() -> {
          Optional.of("").transform(s -> null);
        });

    // If the map operation returns null, Optional.empty() is returned
    // API is more extensive
    // Non-serializable
    assertThat(java.util.Optional.of("").map(s -> null))
        .as("[JDK] Optional returns Optional.empty() when mapping to null")
        .isEmpty()
        .isEqualTo(java.util.Optional.empty());

    // Monadic
    // API is very substantial, tons of compatibility methods with JDK
    // Serializable
    assertThat(Option.of("").map(s -> null).get())
        .as("[Vavr] Optional returns Some(null) when mapping to null")
        .isEqualTo(null);
  }

}

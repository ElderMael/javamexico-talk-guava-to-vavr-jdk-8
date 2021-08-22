package io.eldermael.java.libs.first;

import io.eldermael.java.libs.BaseTestConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TryTest extends BaseTestConfiguration {

  @Test
  @SuppressWarnings("ThrowFromFinallyBlock")
  void shouldThrowRuntimeExceptionInsteadOfIoException() {
    assertThatExceptionOfType(RuntimeException.class)
        .as("Assert Runtime thrown in finally block overrides IOException")
        .isThrownBy(() -> {
          try {
            throw new IOException("This should be thrown");
          } finally {
            throw new RuntimeException("Ouch");
          }
        })
        .withMessage("Ouch");
  }
}

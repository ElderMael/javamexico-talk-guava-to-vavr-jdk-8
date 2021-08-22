package io.eldermael.java.libs;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TryTest extends BaseTestConfiguration {

  @Test
  void shouldThrowRuntimeExceptionInsteadOfIoException() {
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          try {
            throw new IOException("This should be thrown");
          } finally {
            throw new RuntimeException("Ouch");
          }
        });
  }
}

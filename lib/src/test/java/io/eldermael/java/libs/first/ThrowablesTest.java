package io.eldermael.java.libs.first;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Files;
import io.eldermael.java.libs.BaseTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ThrowablesTest extends BaseTestConfiguration {

  @Test
  void shouldPropagateExceptionAsIsWhenExpectedErrorHappensWithGuava() {
    Assertions.assertThatExceptionOfType(NullPointerException.class)
        .as("[Guava] Assert should throw NPE because file URL is null")
        .isThrownBy(() -> {
          String fileName = "does-not-exists.csv";
          try {
            var fileBytes = getFileBytesFromClasspath(fileName);
          } catch (Exception e) {
            Throwables.propagateIfPossible(e, IOException.class, NullPointerException.class);
            log.error("Unknown error while processing file '{}'", fileName, e);
            throw new RuntimeException("Unknown error", e);
          }
        })
        .withMessageContaining("Cannot resolve file 'does-not-exists.csv'")
        .withMessageNotContaining("unknown error");
  }

  @Test
  void shouldPropagateExceptionAsIsWhenExpectedErrorHappensWithJdk() {
    Assertions.assertThatExceptionOfType(NullPointerException.class)
        .as("[JDK] Assert should throw NPE because file URL is null")
        .isThrownBy(() -> {
          String fileName = "does-not-exists.csv";
          try {
            var fileBytes = getFileBytesFromClasspath(fileName);
          } catch (IOException | NullPointerException e) {
            throw e;
          } catch (Exception e) {
            log.error("Unknown error while processing file '{}'", fileName, e);
            throw new RuntimeException("Unknown error", e);
          }
        })
        .withMessageNotContaining("unknown error");
  }

  private byte[] getFileBytesFromClasspath(String fileName) throws IOException {
    var fileUrl = this.getClass().getClassLoader().getResource(fileName);
    var filePath = Preconditions.checkNotNull(
        fileUrl,
        "Cannot resolve file '%s'",
        fileName).getFile();
    return Files.asByteSource(new File(filePath)).read();
  }

}

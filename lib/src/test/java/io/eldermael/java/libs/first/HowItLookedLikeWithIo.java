package io.eldermael.java.libs.first;

import com.google.common.io.Closer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

@Slf4j
@SuppressWarnings("ALL")
public class HowItLookedLikeWithIo {

  @Test
  void shouldCloseStreamsUsingJavaFiveIdioms() {

    // Prepare I/O to read the file
    Closer closer = Closer.create();

    assertThatExceptionOfType(RuntimeException.class)
        .as("[Guava] should close registered Closeables")
        .isThrownBy(() -> {
          // Compose the I/O
          BufferedReader reader = null;
          FileInputStream fileInput = null;
          try {
            fileInput = closer.register(mock(FileInputStream.class));
            reader = closer.register(mock(BufferedReader.class));

            throw new IOException("Booya");

          } catch (IOException e) {
            throw new RuntimeException(e);
          } finally {
            // Closeables.closeQuietly(closer); <- This used to be able to close any Closeable
            try {
              closer.close();
              // throw new IOException("Fail");
            } catch (IOException e) {
              log.error("Error closing resources for file", e);
              throw new AssertionError("Failed to close mocks");
            }
            then(fileInput).should().close();
            then(reader).should().close();
          }

        });


  }

  @Test
  void shouldSuppressExceptionsThrownInTryWithResources() throws IOException {
    FileInputStream inputStream = givenThrowingFileInputStream();
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> {
          try (inputStream) {
            throw new RuntimeException("Thrown by block");
          }
        })
        .withMessage("Thrown by block")
        .withMessageNotContaining("Suppress");

    then(inputStream).should().close();
  }

  @SneakyThrows
  private FileInputStream givenThrowingFileInputStream() {
    FileInputStream newMockInputStream = mock(FileInputStream.class);

    willThrow(new IOException("Suppress"))
        .given(newMockInputStream).close();

    return newMockInputStream;
  }

  private File getFileFromClasspath(String filePath) {
    String fileName = this.getClass().getClassLoader().getResource(filePath).getFile();
    log.info("Reading file '{}'", fileName);
    return new File(fileName);
  }
}

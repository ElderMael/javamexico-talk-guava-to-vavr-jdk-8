package io.eldermael.java.libs.second;

import io.vavr.Lazy;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class HowItLookedLikeUsingVavr {

  // This shows how previous examples look like with Vavr and Streams
  @Test
  void shouldReadLinesFromFileUsingJavaEightIdiomsAndVavr() throws Throwable {

    // Cascade Lazy Calculation Of Autocloseables
    Lazy<FileInputStream> fileInputStream = Lazy
        .of(() -> buildInputStream(getFileFromClasspath("second/file.txt")));

    Lazy<InputStreamReader> inputSTreamReader = fileInputStream.map(this::buildReader);

    Lazy<BufferedReader> bufferedReader = inputSTreamReader.map(this::buildBufferedReader);

    // Try.withResources will close them all a la Try-With-Resources
    var ints = Try.withResources(
            fileInputStream::get,
            inputSTreamReader::get,
            bufferedReader::get
        )
        .of((fis, isr, br) -> br.lines().map(Integer::parseInt).toList()) // Read the lines
        .filter(not(List::isEmpty)) // This will return a Try.Failure if the list is empty
        .getOrElseThrow(throwable -> { // To preserve semantics, convert NoSuchElementException to IllegalStateException
          if (throwable instanceof NoSuchElementException) {
            return new IllegalStateException(throwable);
          }
          // If this is a UncheckedIOException then it's already a RuntimeException
          return throwable;
        })
        .stream() // Stream
        .filter(i -> i > 10) // filter ints
        .toList(); // make a list

    assertThat(ints)
        .as("[Vavr] Assert collection should only contain 20 and 30")
        .containsExactly(20, 30);
  }

  @SneakyThrows
  private BufferedReader buildBufferedReader(InputStreamReader inputStreamReader) {
    return new BufferedReader(inputStreamReader);
  }

  @SneakyThrows
  private FileInputStream buildInputStream(File file) {
    return new FileInputStream(file);
  }

  @SneakyThrows
  private InputStreamReader buildReader(FileInputStream fis) {
    return new InputStreamReader(fis, "UTF-8");
  }

  private File getFileFromClasspath(String filePath) {
    String fileName = this.getClass().getClassLoader().getResource(filePath).getFile();
    log.info("Reading file '{}'", fileName);
    return new File(fileName);
  }
}

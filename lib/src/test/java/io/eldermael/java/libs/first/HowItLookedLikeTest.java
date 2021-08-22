package io.eldermael.java.libs.first;

import ch.lambdaj.Lambda;
import ch.lambdaj.collection.LambdaCollections;
import ch.lambdaj.function.closure.Closure;
import ch.lambdaj.function.convert.Converter;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import io.eldermael.java.libs.BaseTestConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static ch.lambdaj.Lambda.var;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * We had tons of loops that we refactored to LambaJ
 */
@Slf4j
@SuppressWarnings("ALL")
public class HowItLookedLikeTest extends BaseTestConfiguration {

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdioms() {
    String fileName = this.getClass().getClassLoader().getResource("first/batchfile.txt").getFile();
    log.info("Reading file '{}'", fileName);
    File batchFile = new File(fileName);

    BufferedReader reader = null;
    FileInputStream fileInput = null;

    try {

      fileInput = new FileInputStream(batchFile);
      reader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));

      List<String> batchFileLines = new LinkedList<String>();
      String line = null;

      while ((line = reader.readLine()) != null) {
        batchFileLines.add(line);
      }

      if (batchFileLines.size() == 0) {
        throw new IllegalStateException("Batch file '" + fileName + "' has no lines");
      }

      List<Integer> ints = new LinkedList<Integer>();

      for (int i = 0; i < batchFileLines.size(); i++) {
        Integer parsed = Integer.parseInt(batchFileLines.get(i));
        if (parsed > 10) {
          ints.add(parsed);
        }
      }

      assertThat(ints)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);


    } catch (FileNotFoundException e) {
      throw new RuntimeException("Batch file '" + fileName + "' does not exists", e);
    } catch (IOException e) {
      throw new RuntimeException("Error opening file '" + fileName + "'", e);
    } finally {
      // If you thow any exceptions in the finally block, they override the exceptions thrown in the
      // try block
      if (fileInput != null) {
        try {
          fileInput.close();
        } catch (IOException e) {
          log.error("Exception thrown while opening file '{}'", fileName, e);
        }
      }

      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          log.error("Exception thrown while closing file '{}'", fileName, e);
        }
      }
    }

  }

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdiomsPlusGuava() {
    String fileName = this.getClass().getClassLoader().getResource("first/batchfile.txt").getFile();
    log.info("Reading file '{}'", fileName);
    File batchFile = new File(fileName);

    try {
      List<String> batchFileLines = Files.readLines(batchFile, Charsets.UTF_8);

      Preconditions.checkState(
          batchFileLines.size() > 0,
          "Batch file '%s' has no lines",
          batchFile.getName()
      );

      List<Integer> ints = Lists.transform(batchFileLines, new Function<String, Integer>() {
        @Override
        public @Nullable Integer apply(@Nullable String input) {
          return Integer.parseInt(input);
        }
      });


      Iterable<Integer> greaterThanTen = Iterables.filter(ints, new Predicate<Integer>() {
        @Override
        public boolean apply(@Nullable Integer input) {
          return input > 10;
        }
      });

      assertThat(greaterThanTen)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

  }

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdiomsPlusGuavaAndLambdaJ() {
    try {
      String fileName = this.getClass().getClassLoader().getResource("first/batchfile.txt").getFile();
      log.info("Reading file '{}'", fileName);
      File batchFile = new File(fileName);

      List<String> batchFileLines = Files.readLines(
          batchFile,
          Charsets.UTF_8
      );

      Preconditions.checkState(
          batchFileLines.size() > 0,
          "Batch file '%s' has no lines",
          batchFile.getName()
      );

      Closure toInt = Lambda.closure().of(Integer.class, "parseInt", var(String.class));

      List<Integer> ints = Lambda.convert(batchFileLines, (Converter<String, Integer>) toInt.cast(Converter.class));

      List<Integer> greaterThanTen = Lambda.filter(greaterThan(10), ints);

      assertThat(greaterThanTen)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);

    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdiomsPlusGuavaAndLambdaJAndLambdaCollection() {
    String fileName = this.getClass().getClassLoader().getResource("first/batchfile.txt").getFile();
    log.info("Reading file '{}'", fileName);
    File batchFile = new File(fileName);

    try {
      List<String> batchFileLines = Files.readLines(
          batchFile,
          Charsets.UTF_8
      );

      Preconditions.checkState(
          batchFileLines.size() > 0,
          "Batch file '%s' has no lines",
          batchFile.getName()
      );

      Closure toInt = Lambda.closure().of(Integer.class, "parseInt", var(String.class));

      List<Integer> greaterThanTen = LambdaCollections
          .with(batchFileLines)
          .convert((Converter<String, Integer>) toInt.cast(Converter.class))
          .retain(greaterThan(10));

      assertThat(greaterThanTen)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);

    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

  }

}


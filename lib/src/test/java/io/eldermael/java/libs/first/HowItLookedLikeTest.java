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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static ch.lambdaj.Lambda.convert;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.var;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;

@Slf4j
@SuppressWarnings("ALL")
public class HowItLookedLikeTest extends BaseTestConfiguration {

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdioms() {
    // Get a file
    File batchFile = getFileFromClasspath("first/batchfile.txt");

    // Prepare I/O to read the file
    BufferedReader reader = null;
    FileInputStream fileInput = null;

    try {

      // Compose the I/O
      fileInput = new FileInputStream(batchFile);
      reader = new BufferedReader(new InputStreamReader(fileInput, "UTF-8"));

      // Read the lines
      List<String> batchFileLines = new LinkedList<String>();
      String line = null;

      while ((line = reader.readLine()) != null) {
        batchFileLines.add(line);
      }

      // Check that the file contains lines
      if (batchFileLines.size() == 0) {
        throw new IllegalStateException("Batch file '" + batchFile + "' has no lines");
      }

      // Convert the lines to data types and filter
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

      // Propagate errors to next layer
    } catch (IOException e) {
      throw new RuntimeException("Error opening file '" + batchFile + "'", e);
    } finally {
      // If you thow any exceptions in the finally block, they override the exceptions thrown in the
      // try block, thus you need to suppress them and log them at least
      if (fileInput != null) {
        try {
          fileInput.close();
        } catch (IOException e) {
          log.error("Exception thrown while opening file '{}'", batchFile, e);
        }
      }

      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {
          log.error("Exception thrown while closing file '{}'", batchFile, e);
        }
      }
    }

  }

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdiomsPlusGuava() {
    // Get the file
    File batchFile = getFileFromClasspath("first/batchfile.txt");

    try {
      // Ignore this is reading the whole file into memory
      List<String> batchFileLines = Files.readLines(batchFile, Charsets.UTF_8);

      // Preconditions are guard clauses
      Preconditions.checkState(
          batchFileLines.size() > 0,
          "Batch file '%s' has no lines",
          batchFile.getName()
      );

      // Higher order functions for Lists
      List<Integer> ints = Lists.transform(batchFileLines, new Function<String, Integer>() {
        @Override
        public @Nullable Integer apply(@Nullable String input) {
          return Integer.parseInt(input);
        }
      });


      // Iterables or Collections2
      //Collection<Integer> greaterThanTen = Collections2.filter(ints,  new Predicate<Integer>() {
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
      File batchFile = getFileFromClasspath("first/batchfile.txt");

      List<String> batchFileLines = Files.readLines(
          batchFile,
          Charsets.UTF_8
      );

      Preconditions.checkState(
          batchFileLines.size() > 0,
          "Batch file '%s' has no lines",
          batchFile.getName()
      );

      // Point to object/static methods for futher use
      Closure toInt = Lambda.closure().of(Integer.class, "parseInt", var(String.class));

      // Closures can be cast to any Functional Inteface i.e. any one-method inteface
      List<Integer> ints = convert(batchFileLines, (Converter<String, Integer>) toInt.cast(Converter.class));

      // Filtaring, projection and aggregation can use Hamcrest matchers
      // This simplifies code greatly
      List<Integer> greaterThanTen = filter(greaterThan(10), ints);

      assertThat(greaterThanTen)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);

    } catch (IOException e) {
      // This will propagate as RuntimeException
      // No longer recommended
      throw Throwables.propagate(e);
    } catch (Exception e) {
      // This will propagate too
      throw Throwables.propagate(e);
    }
  }

  @Test
  void shouldReadLinesFromFileUsingJavaFiveIdiomsPlusGuavaAndLambdaJAndLambdaCollection() {
    File batchFile = getFileFromClasspath("first/batchfile.txt");

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

      // Nesting functions gets hard to read the more you add
      List<Integer> greaterThanTen = filter(
          greaterThan(10),
          convert(batchFileLines, (Converter<String, Integer>) toInt.cast(Converter.class))
      );

      // Fluent way is more readable with many operations
      List<Integer> greaterThanTenFluent = LambdaCollections
          .with(batchFileLines)
          .convert((Converter<String, Integer>) toInt.cast(Converter.class))
          .retain(greaterThan(10));

      assertThat(greaterThanTen)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);

      assertThat(greaterThanTenFluent)
          .as("Assert collection should only contain 20 and 30")
          .containsExactly(20, 30);

    } catch (IOException e) {
      throw Throwables.propagate(e);
    }

  }

  private File getFileFromClasspath(String filePath) {
    String fileName = this.getClass().getClassLoader().getResource(filePath).getFile();
    log.info("Reading file '{}'", fileName);
    return new File(fileName);
  }

}


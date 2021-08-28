package io.eldermael.java.libs.first;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import io.eldermael.java.libs.BaseTestConfiguration;
import io.eldermael.java.libs.Record;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static io.eldermael.java.libs.Record.sampleData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

public class HowItLookedLikeWithCollections extends BaseTestConfiguration {

  // Filtering
  @Test
  void shouldFilterRecordsWithCodeSsUsingGuava() {

    Iterable<Record> recordsHavingCodeWithDoubleS = Iterables.filter(sampleData(), new Predicate<Record>() {
      @Override
      public boolean apply(@Nullable Record input) {
        return input.getCode().contains("SS");
      }
    });

    Iterable<Double> amountsOnRecords = Iterables.transform(recordsHavingCodeWithDoubleS, new Function<Record, Double>() {
      @Override
      public @Nullable Double apply(@Nullable Record input) {
        return input.getAmount();
      }
    });

    assertThat(recordsHavingCodeWithDoubleS)
        .as("[LambdaJ] should contain SSS and SSA")
        .allMatch(r -> r.getCode().contains("SS"));

    assertThat(amountsOnRecords)
        .as("[LambdaJ] should contain 200 and 200")
        .containsExactly(200.0, 200.0);

  }

  // LambdaJ
  @Test
  void shouldFilterRecordsWithCodeSsUsingLambdaJ() {

    List<Record> recordsHavingCodeWithDoubleS = filter(
        having(on(Record.class).getCode(), containsString("SS")),
        sampleData()
    );

    // List<Record> recordsHavingCodeWithDoubleS = select(sampleData(), having(on(Record.class).getCode(), containsString("SS")));

    List<Double> amountsOnRecords = extract(recordsHavingCodeWithDoubleS, on(Record.class).getAmount());

    assertThat(recordsHavingCodeWithDoubleS)
        .as("[LambdaJ] should contain SSS and SSA")
        .allMatch(r -> r.getCode().contains("SS"));

    assertThat(amountsOnRecords)
        .as("[LambdaJ] should contain 200 and 200")
        .containsExactly(200.0, 200.0);
  }

  // Java Streams
  @Test
  void shouldFilterRecordsWithCodeSsUsingStreams() {
    var recordsHavingCodeWithDoubleS = sampleData().stream()
        .filter(r -> r.getCode().contains("SS"))
        .toList();

    var amountsOnRecords = recordsHavingCodeWithDoubleS.stream()
        .map(Record::getAmount)
        .toList();

    assertThat(recordsHavingCodeWithDoubleS)
        .as("[LambdaJ] should contain SSS and SSA")
        .allMatch(r -> r.getCode().contains("SS"));

    assertThat(amountsOnRecords)
        .as("[LambdaJ] should contain 200 and 200")
        .containsExactly(200.0, 200.0);
  }

  // Commons Collections
  // https://commons.apache.org/proper/commons-collections/javadocs/api-4.4/index.html
  @Test
  void shouldMutateCollectionUsingApacheCommons() {
    // Need to create a mutable list first
    List<Record> mutableSampleData = new LinkedList<>();

    // Then shallow copy the contents
    mutableSampleData.addAll(sampleData());

    // Execute the mutable filter on the copy
    Boolean successful = CollectionUtils.filter(mutableSampleData,
        new org.apache.commons.collections4.Predicate<Record>() {
          @Override
          public boolean evaluate(Record object) {
            return object.getCode().contains("SS");
          }
        });

    // Verify the mutation happened correctly
    assertThat(successful).isTrue();

    // Create a new mutable target list
    List<Double> transformed = new LinkedList<>();

    // Execute a transformation that will copy the transformed element
    // to a new list
    var transformed2 = CollectionUtils.collect(mutableSampleData, new Transformer<Record, Double>() {
      @Override
      public Double transform(Record input) {
        return input.getAmount();
      }
    }, transformed);

    // Check the transformation happened somehow, as the previous
    // method does not return boolean or something
    assertThat(transformed)
        .as("[Commons] should contain 200 and 200")
        .containsExactly(200.0, 200.0);

    assertThat(transformed2)
        .as("[Commons] should contain 200 and 200")
        .containsExactly(200.0, 200.0)
        .isEqualTo(transformed);
  }
}

package io.eldermael.java.libs.first;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import io.eldermael.java.libs.BaseTestConfiguration;
import io.eldermael.java.libs.Record;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

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

    Iterables.filter(sampleData(), new Predicate<Record>() {
      @Override
      public boolean apply(@Nullable Record input) {
        return false;
      }
    });

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
}

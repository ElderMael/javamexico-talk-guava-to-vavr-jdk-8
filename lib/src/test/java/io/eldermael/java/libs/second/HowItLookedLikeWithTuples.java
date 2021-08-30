package io.eldermael.java.libs.second;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HowItLookedLikeWithTuples {

  @Test
  void shouldShowDifferenceWithTuples() {
    // Create a Pair
    Pair<String, String> javaTuples = Pair.with("a", "b");
    Tuple2<String, String> vavr = Tuple.of("a", "b");

    // when
    Triplet<String, String, String> triplet = javaTuples.add("c");
    Tuple3<String, String, String> vavrTriplet = vavr.append("c");

    // Then
    assertThat(triplet.containsAll("a", "b", "c")).isTrue();

    // Or, as JavaTuples can be converted to other types more conveniently
    assertThat(triplet.toList()).contains("a", "b", "c");


    List<String> apply = vavrTriplet.apply(List::of);
    assertThat(apply).contains("a", "b", "c");

  }

}

package io.eldermael.java.libs;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Record {

  private String name;
  private String code;
  private Double amount;
  private String description;

  public static List<Record> sampleData() {

    return List.of(
        Record.builder().name("XLD").code("SSS").amount(200.00).description("Amount for X").build(),
        Record.builder().name("SLD").code("SSA").amount(200.00).description("Amount for A").build(),
        Record.builder().name("ALD").code("SDB").amount(200.00).description("Amount for B").build(),
        Record.builder().name("ZLD").code("SCD").amount(200.00).description("Amount for D").build()
    );

  }
}

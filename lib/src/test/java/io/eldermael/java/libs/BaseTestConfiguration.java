package io.eldermael.java.libs;

import org.assertj.core.api.Assertions;
import org.assertj.core.description.Description;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class BaseTestConfiguration {

  private static final Logger log = LoggerFactory.getLogger("io.eldermael.java.libs.test");
  private static final StringBuffer descriptionReportBuilder = new StringBuffer();


  @BeforeAll
  public static void configureAssertion() {
    descriptionReportBuilder.setLength(0);
    descriptionReportBuilder.append(String.format("Assertions:%n"));
    Consumer<Description> descriptionConsumer = desc -> descriptionReportBuilder.append(String.format("-- %s%n", desc));
    Assertions.setDescriptionConsumer(descriptionConsumer);
  }

  @AfterAll
  public static void printReport() {
    log.info(descriptionReportBuilder.toString());
  }

}

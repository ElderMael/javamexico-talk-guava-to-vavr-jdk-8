package io.eldermael.java.libs;

import java.util.concurrent.atomic.AtomicInteger;

public class MockProcesses {

  private static AtomicInteger processNumber = new AtomicInteger(1);


  public static Integer successfulProcess() {
    processNumber.incrementAndGet();
    return 0;
  }

  public static Integer failedProcess() {
    throw new RuntimeException("Process # " + processNumber.incrementAndGet() + " failed.");
  }

}

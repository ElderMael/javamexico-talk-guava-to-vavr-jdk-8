package io.eldermael.java.libs;

public interface EmailQueue {

  ProcessResult persistEmailForLater(String message);


}

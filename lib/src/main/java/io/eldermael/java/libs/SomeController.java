package io.eldermael.java.libs;

import lombok.AllArgsConstructor;

import java.net.http.HttpRequest;
import java.util.concurrent.Future;

@AllArgsConstructor
public class SomeController {

  AsyncService service;

  public Future<?> processRequest(HttpRequest request) {
    return service.submitBusinessProcess(new BusinessModel());
  }

}

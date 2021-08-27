package io.eldermael.java.libs;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AsyncEmailSendingService {

  private final ListeningExecutorService executor;

  public ListenableFuture<ProcessResult> sendEmail(String email) {
    return executor.submit(() -> ProcessResult.SUCCESS);
  }

}

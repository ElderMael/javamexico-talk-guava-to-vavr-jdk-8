package io.eldermael.java.libs;

import com.google.common.util.concurrent.ListenableFuture;

public interface AsyncService {

  ListenableFuture<String> submitBusinessProcess(BusinessModel model);

}

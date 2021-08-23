package io.eldermael.java.libs;


import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.vavr.concurrent.Future;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class FuturesTest extends BaseTestConfiguration {

  private static final Logger log = LoggerFactory.getLogger(FuturesTest.class);

  // Guava's ListenableFutures are created from this wrapper
  // or SettableFuture
  private static final ListeningExecutorService wrappedExecutor = MoreExecutors
      .listeningDecorator(ForkJoinPool.commonPool());

  @Test
  void guavaFutureExample() {
    var listenableFuture = wrappedExecutor.submit(MockProcesses::successfulProcess);

    var settableFuture = SettableFuture.create();

    listenableFuture.addListener(() -> {
      try {
        var exitCode = listenableFuture.get();

        settableFuture.set(exitCode);
        settableFuture.setFuture(listenableFuture);

      } catch (InterruptedException | ExecutionException e) {
        throw new AssertionError("Guava future did not complete correctly");
      }
    }, wrappedExecutor);

    await()
        .untilAsserted(() -> {
          assertThat(settableFuture)
              .as("Settable Future is completed with the callback.")
              .isNotCancelled()
              .isDone();
        });

  }

  @Test
  void howToCreateFuturesInEachLibrary() {
    // Guava
    // Created from the decorator
    var listenableFuture = wrappedExecutor.submit(MockProcesses::successfulProcess);

    // Completable Future
    // Uses ForkJoinPool.commonPool() too if parallelism > 1
    var completableFuture = CompletableFuture.supplyAsync(MockProcesses::successfulProcess);

    // Vavr
    // Uses ForkJoinPool.commonPool() under the covers
    var vavrFuture = Future.of(MockProcesses::successfulProcess);

    await()
        .untilAsserted(() -> {
          assertThat(listenableFuture)
              .as("ListenableFuture is completed")
              .isDone()
              .has(successfulProcess)
              .isNotCancelled();

          assertThat(completableFuture)
              .as("CompletableFuture is completed")
              .isCompleted()
              .isCompletedWithValue(0)
              .isNotCancelled()
              .isDone();

          assertThat(vavrFuture.toCompletableFuture())
              .as("Vavr Future is completed")
              .isCompleted()
              .isCompletedWithValue(0)
              .isNotCancelled();
        });


  }

  @Test
  void howToComposeFuturesInEachLibrary() {
    // Guava
    // Compose Guava Future
    var listenableProcess = wrappedExecutor.submit(MockProcesses::successfulProcess);
    var listenableProcessResult = Futures
        .transform(
            listenableProcess,
            ProcessResult::fromExitCode,
            wrappedExecutor);

    // Java 8
    // Compose Completable Future
    var completableFuture = CompletableFuture
        .supplyAsync(MockProcesses::successfulProcess)
        .thenApply(ProcessResult::fromExitCode);

    // Vavr
    var vavrProcessResult = Future
        .of(MockProcesses::successfulProcess)
        .map(ProcessResult::fromExitCode);

    await()
        .untilAsserted(() -> {
          assertThat(listenableProcessResult)
              .as("Guava future mapping to ProcessResult")
              .isDone()
              .has(successfulProcessResult)
              .isNotCancelled();

          assertThat(completableFuture)
              .as("Completable Future mapping to ProcessResult")
              .isCompleted()
              .isCompletedWithValue(ProcessResult.SUCCESS)
              .isNotCancelled();

          assertThat(vavrProcessResult.toCompletableFuture())
              .as("Vavr Future mapping to ProcessResult")
              .isCompleted()
              .isCompletedWithValue(ProcessResult.SUCCESS)
              .isNotCancelled();
        });


  }

  private final Condition<java.util.concurrent.Future<Integer>> successfulProcess =
      new Condition<java.util.concurrent.Future<Integer>>((result) -> {
        try {
          return result.get().equals(0);
        } catch (InterruptedException | ExecutionException e) {
          throw new AssertionError(e);
        }
      }, "returned a zero exit code");

  private final Condition<java.util.concurrent.Future<ProcessResult>> successfulProcessResult =
      new Condition<java.util.concurrent.Future<ProcessResult>>((result) -> {
        try {
          return result.get().equals(ProcessResult.SUCCESS);
        } catch (InterruptedException | ExecutionException e) {
          throw new AssertionError(e);
        }
      }, "returned successful process result");
}

package io.eldermael.java.libs.first;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.eldermael.java.libs.EmailSender;
import io.eldermael.java.libs.MailException;
import io.eldermael.java.libs.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Slf4j
public class HowItLookedLikeWithConcurrency {

  private EmailSender mailSender = new EmailSender();
  private ExecutorService executor = Executors.newFixedThreadPool(3);
  private ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);

  // Composing Futures in Java 1.5 was a pain
  @Test
  void shouldComposeFutureUsingJavaFiveIdioms() {
    String message = "Oopsie!";

    Future<ProcessResult> emailResult = executor.submit(new Callable<ProcessResult>() {
      @Override
      public ProcessResult call() throws Exception {
        mailSender.sendAlertEmail(message);
        return ProcessResult.SUCCESS;
      }
    });

    Future<?> alertSentOrQueued = executor.submit(new Callable<ProcessResult>() {
      @Override
      public ProcessResult call() throws Exception {
        try {
          ProcessResult result = emailResult.get();

          if (result.equals(ProcessResult.SUCCESS)) {
            return result;
          }

          return persistEmailForLater(message);
        } catch (ExecutionException e) {
          // Checking if thrown exception is an instance of what we expect
          if (Throwables.getRootCause(e) instanceof MailException) {
            return persistEmailForLater(message);
          }

          return ProcessResult.ERROR;
        } catch (Exception e) {
          log.error("Unknown error sending email, ", e);
          return ProcessResult.ERROR;
        }
      }
    });

    await()
        .untilAsserted(() -> {
          assertThat(alertSentOrQueued)
              .as("[JDK] Assert email is sent or queued successfully")
              .isNotCancelled()
              .isDone();

          assertThat(alertSentOrQueued.get())
              .isEqualTo(ProcessResult.SUCCESS);
        });

  }

  // Guava provides a way to catch exceptions by composing on errors
  // Futures.transform also composes
  @Test
  void shouldComposeFutureUsingGuavaListenableFuture() {
    String message = "Oopsie!";

    ListenableFuture<ProcessResult> emailResult = listeningExecutor.submit(new Callable<ProcessResult>() {
      @Override
      public ProcessResult call() throws Exception {
        mailSender.sendAlertEmail(message);
        return ProcessResult.SUCCESS;
      }
    });

    ListenableFuture<ProcessResult> alertSentOrQueued = Futures.catchingAsync(
        emailResult,
        MailException.class,
        new AsyncFunction<MailException, ProcessResult>() {
          @Override
          public @Nullable ListenableFuture<ProcessResult> apply(@Nullable MailException input) {
            return Futures.immediateFuture(persistEmailForLater(message));
          }
        }, listeningExecutor);

    await()
        .untilAsserted(() -> {
          assertThat(alertSentOrQueued)
              .as("[JDK] Assert email is sent or queued successfully")
              .isNotCancelled()
              .isDone();

          assertThat(alertSentOrQueued.get())
              .isEqualTo(ProcessResult.SUCCESS);
        });

  }

  // Everything is easier with newer Java versions
  @Test
  void shouldComposeOnFailingFutureWithJdk() {
    var message = "Oopsie!";
    var alertSentOrQueued = CompletableFuture
        .supplyAsync(() -> {
          mailSender.sendAlertEmail(message);
          return ProcessResult.SUCCESS;
        }, executor).exceptionallyAsync(throwable -> {
          if (throwable.getCause() instanceof MailException) {
            return persistEmailForLater(message);
          }
          return ProcessResult.ERROR;
        }, executor);

    await()
        .untilAsserted(() -> {
          assertThat(alertSentOrQueued)
              .as("[JDK] Assert email is sent or queued successfully")
              .isNotCancelled()
              .isDone();

          assertThat(alertSentOrQueued.get())
              .isEqualTo(ProcessResult.SUCCESS);
        });
  }

  private ProcessResult persistEmailForLater(String message) {
    return ProcessResult.SUCCESS;
  }

}

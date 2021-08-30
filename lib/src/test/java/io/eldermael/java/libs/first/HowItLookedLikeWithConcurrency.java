package io.eldermael.java.libs.first;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.eldermael.java.libs.EmailQueue;
import io.eldermael.java.libs.EmailSender;
import io.eldermael.java.libs.MailException;
import io.eldermael.java.libs.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class HowItLookedLikeWithConcurrency {

  private EmailSender mailSender = new EmailSender();
  private ExecutorService executor = Executors.newFixedThreadPool(3);
  private ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executor);

  @Mock()
  EmailQueue mailQueue;

  @BeforeEach
  void shouldSendEmailToQueueSuccessfully() {
    given(mailQueue.persistEmailForLater(any()))
        .willReturn(ProcessResult.SUCCESS);
  }

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
          // Will throw an ExecutionException wrapping MailException
          ProcessResult result = emailResult.get();

          if (result.equals(ProcessResult.SUCCESS)) {
            return result;
          }

          return mailQueue.persistEmailForLater(message);
        } catch (ExecutionException e) {
          // Checking if thrown exception is an instance of what we expect
          if (Throwables.getRootCause(e) instanceof MailException) {
            return mailQueue.persistEmailForLater(message);
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

          then(mailQueue).should().persistEmailForLater(any());
        });

  }

  // Guava provides a way to catch exceptions by composing on errors
  // Futures.transform also composes
  @Test
  void shouldComposeFutureUsingGuavaListenableFuture() {
    String message = "Oopsie!";

    // Create a Listenable from a faulty process
    // Anonymous classes all over the place
    ListenableFuture<ProcessResult> emailResult = listeningExecutor.submit(new Callable<ProcessResult>() {
      @Override
      public ProcessResult call() throws Exception {
        mailSender.sendAlertEmail(message);
        return ProcessResult.SUCCESS;
      }
    });

    // If the faulty process has issues, fallback to another process
    ListenableFuture<ProcessResult> alertSentOrQueued = Futures.catchingAsync( // Futures.withFallback
        emailResult,
        MailException.class,
        new AsyncFunction<MailException, ProcessResult>() {
          @Override
          public @Nullable ListenableFuture<ProcessResult> apply(@Nullable MailException input) {
            return Futures.immediateFuture(mailQueue.persistEmailForLater(message));
          }
        }, listeningExecutor);

    // This is going to be successful by queueing the mail
    await()
        .untilAsserted(() -> {
          assertThat(alertSentOrQueued)
              .as("[JDK] Assert email is sent or queued successfully")
              .isNotCancelled()
              .isDone();

          assertThat(alertSentOrQueued.get())
              .isEqualTo(ProcessResult.SUCCESS);

          then(mailQueue).should().persistEmailForLater(any());
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
        }, executor)
        .exceptionallyAsync(throwable -> {
          if (throwable.getCause() instanceof MailException) {
            return mailQueue.persistEmailForLater(message);
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

          then(mailQueue).should().persistEmailForLater(any());
        });
  }

}

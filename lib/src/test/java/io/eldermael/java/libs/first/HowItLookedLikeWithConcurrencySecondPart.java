package io.eldermael.java.libs.first;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.eldermael.java.libs.AsyncEmailSendingService;
import io.eldermael.java.libs.AsyncService;
import io.eldermael.java.libs.MockProcesses;
import io.eldermael.java.libs.ProcessResult;
import io.eldermael.java.libs.SomeController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpRequest;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static io.eldermael.java.libs.ProcessResult.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class HowItLookedLikeWithConcurrencySecondPart {

  @Mock
  ListeningExecutorService mockExecutor;

  @Mock
  AsyncService mockService;

  @InjectMocks
  AsyncEmailSendingService emailService;

  @InjectMocks
  SomeController controller;

  /**
   * This will create a LES that will run tasks in the same thread
   * that invokes ListeningExecutorService#submit
   * <p>
   * Used to be called "sameThreadExecutor()"
   */
  final ListeningExecutorService directExecutor = MoreExecutors.listeningDecorator(MoreExecutors.newDirectExecutorService());


  // Making sure that this tests uses the same thread always by
  // submitting runnables/callables to an executor that does not run
  // tasks in different threads
  @Test
  void shouldUseSameThreadToRunTasksSubmitted() throws ExecutionException, InterruptedException {

    var testThread = Thread.currentThread(); // Get a hold of the current thread

    ListenableFuture<Thread> executorThreadFuture = directExecutor
        .submit(Thread::currentThread); // Get the thread running the task

    assertThat(executorThreadFuture)
        .isDone()
        .isNotCancelled();

    var executorThread = executorThreadFuture.get();

    // Asserting that the thread running the task is the same as the
    // thread running the tests
    assertThat(testThread)
        .isEqualTo(executorThread);

  }

  // We did not have CompletableFuture during those days and
  // mocking Future was dangerous
  @Test
  void shouldUseSettableFutureToBlockTestUntilFinished() throws ExecutionException, InterruptedException {
    // given
    SettableFuture<Integer> result = SettableFuture.create();
    result.set(0); // Just make sure this is set before calling Future.get()
    given(mockExecutor.submit(Mockito.<Callable<Integer>>any()))
        .willReturn(result);

    // when
    Integer process = mockExecutor.submit(MockProcesses::successfulProcess).get();

    // then
    assertThat(process)
        .isEqualTo(0);
  }

  // Alternatively, we could pass executors as dependencies
  // This works for mocks and for MoreExecutors.newDirectExecutorService()
  @Test
  void shouldReturnSettableFutureFromMockedExecutorService() throws ExecutionException, InterruptedException {
    // given
    SettableFuture<ProcessResult> result = SettableFuture.create();
    result.set(SUCCESS); // Just make sure this is set before calling Future.get()
    given(mockExecutor.submit(Mockito.<Callable<ProcessResult>>any()))
        .willReturn(result);

    // when
    ProcessResult emailProcess = emailService.sendEmail("Hello!").get();

    // then
    BDDMockito.then(mockExecutor)
        .should().submit(any(Callable.class));

    assertThat(emailProcess)
        .isEqualTo(SUCCESS);
  }

  // Way better, mock dependencies returning Futures and return the
  // SettableFuture from the test :)
  @Test
  void shouldReturnCompletedFutureFromDependencyIfNoExecutorIsGiven() throws ExecutionException, InterruptedException {
    // given
    SettableFuture<String> result = SettableFuture.create();
    result.set("Finished");
    given(mockService.submitBusinessProcess(any()))
        .willReturn(result);

    // when
    Object mockedResult = controller.processRequest(mock(HttpRequest.class)).get();

    // then
    then(mockService).should().submitBusinessProcess(any());
    assertThat(mockedResult)
        .isInstanceOf(String.class)
        .isEqualTo("Finished");
  }
}

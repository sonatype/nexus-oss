package org.sonatype.nexus.test.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Executes a batch of {@link ConcurrentTask}s concurrently for testing purposes.
 */
public class ConcurrentRunner
    extends ComponentSupport
{
  private List<ConcurrentTask> tasks = new ArrayList<>();

  // A cyclic barrier ensures that the threads align after each unit of work, to avoid some completing much before
  // the others, thereby increasing contention
  private CyclicBarrier startingGun;

  // A signal to get test workers to abort if a peer finds a failure
  private final AtomicBoolean failureHasOccurred = new AtomicBoolean(false);

  private final int iterations;

  private final int iterationTimeoutSeconds;

  private AtomicInteger runInvocations = new AtomicInteger(0);

  /**
   * @param iterations              How many times each task should be invoked
   * @param iterationTimeoutSeconds How long each iteration is given for all tasks to complete
   */
  public ConcurrentRunner(int iterations, int iterationTimeoutSeconds) {
    this.iterations = iterations;
    this.iterationTimeoutSeconds = iterationTimeoutSeconds;
  }

  /**
   * Adds a task to the runner, which will be executed 'iterations' times by a single thread.
   */
  public void addTask(final ConcurrentTask task) {
    addTask(1, task);
  }

  /**
   * Adds one or more instances of a task to the runner. The task isn't cloned but used by multiple threads, and so
   * tasks provided to this method should be stateless and thread-safe.
   */
  public void addTask(final int instances, final ConcurrentTask task)
  {
    Preconditions.checkArgument(instances >= 1);
    for (int i = 0; i < instances; i++) {
      tasks.add(task);
    }
  }

  /**
   * Invokes the tasks 'iteration' times.
   *
   * @throws Exception an exception thrown by one of the tasks
   */
  public void go() throws Exception {
    runInvocations.set(0);

    startingGun = new CyclicBarrier(tasks.size(), new Runnable()
    {
      int iterationCount = 0;

      @Override
      public void run() {
        log.debug("Iteration starting: " + iterationCount++);
      }
    });

    List<ConcurrentTestWorker> testWorkers = buildTaskWorkers();

    final List<Future<Void>> taskResults = executeTasksConcurrently(testWorkers);

    failIfTasksWereCancelled(taskResults);

    failIfTasksCaughtExceptions(testWorkers);
  }

  private List<ConcurrentTestWorker> buildTaskWorkers() {
    List<ConcurrentTestWorker> testWorkers = new ArrayList<>();

    final ConcurrentTestContext context = new ConcurrentTestContext(startingGun, failureHasOccurred,
        iterations, runInvocations, iterationTimeoutSeconds);

    for (int workerNumber = 0; workerNumber < tasks.size(); workerNumber++) {
      final ConcurrentTestWorker worker = new ConcurrentTestWorker(workerNumber, tasks.get(workerNumber), context);
      testWorkers.add(worker);
    }
    return testWorkers;
  }

  private List<Future<Void>> executeTasksConcurrently(final List<ConcurrentTestWorker> testWorkers)
      throws InterruptedException
  {
    ExecutorService service = Executors.newFixedThreadPool(tasks.size());
    final List<Future<Void>> futures = service
        .invokeAll(testWorkers, iterationTimeoutSeconds * iterations, TimeUnit.SECONDS);
    service.shutdown();
    return futures;
  }


  private void failIfTasksWereCancelled(final List<Future<Void>> taskResults) {
    int cancelledCount = 0;
    for (Future<Void> taskResult : taskResults) {
      if (taskResult.isCancelled()) {
        cancelledCount++;
      }
    }
    if (cancelledCount > 0) {
      throw new IllegalStateException(cancelledCount + " test workers cancelled due to timeout");
    }
  }

  private void failIfTasksCaughtExceptions(final List<ConcurrentTestWorker> testWorkers) throws Exception {
    for (ConcurrentTestWorker testWorker : testWorkers) {
      if (testWorker.experiencedFailure()) {
        // Throws any old exception, but also JUnit assertion errors
        log.info("Attempting to throw ", testWorker.getException());
        Throwables.propagateIfPossible(testWorker.getException(), Exception.class, AssertionError.class);
      }
    }
  }

  /**
   * The total number of times a {@link ConcurrentTask}'s {@link ConcurrentTask#run()} method was invoked.
   */
  public int getRunInvocations() {
    return runInvocations.get();
  }

  /**
   * The number of tasks added to the runner, each one invoked by a separate thread.
   */
  public int getTaskCount() {
    return tasks.size();
  }

  /**
   * The number of times each task-instance will be invoked.
   */
  public int getIterations() {
    return iterations;
  }
}

package org.sonatype.nexus.test.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

  private final int terminationWaitTimeSeconds;

  private AtomicInteger runInvocations = new AtomicInteger(0);

  /**
   * @param iterations                 How many times each task should be invoked
   * @param terminationWaitTimeSeconds How long to wait for all iterations to complete
   */
  public ConcurrentRunner(int iterations, int terminationWaitTimeSeconds) {
    this.iterations = iterations;
    this.terminationWaitTimeSeconds = terminationWaitTimeSeconds;
  }

  public void addTask(final ConcurrentTask task) {
    addTask(1, task);
  }

  /**
   * Adds a task to the runner. The task is executed 'threads' number of times each iteration.
   */
  public void addTask(final int threads, final ConcurrentTask task)
  {
    Preconditions.checkArgument(threads >= 1);
    for (int i = 0; i < threads; i++) {
      tasks.add(task);
    }
  }

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

    List<ConcurrentTestWorker> testWorkers = new ArrayList<>();

    ExecutorService service = Executors.newFixedThreadPool(tasks.size());

    for (ConcurrentTask task : tasks) {
      final ConcurrentTestWorker worker = new ConcurrentTestWorker(testWorkers.size(), startingGun, failureHasOccurred,
          iterations, task, runInvocations);
      testWorkers.add(worker);
    }

    for (Runnable r : testWorkers) {
      service.submit(r);
    }

    service.shutdown();
    service.awaitTermination(terminationWaitTimeSeconds, TimeUnit.SECONDS);

    for (ConcurrentTestWorker testWorker : testWorkers) {
      if (testWorker.experiencedFailure()) {
        // Throws any old exception, but also JUnit assertion errors
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
}

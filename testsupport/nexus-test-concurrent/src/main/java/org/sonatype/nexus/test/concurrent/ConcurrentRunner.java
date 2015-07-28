package org.sonatype.nexus.test.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Preconditions;
import org.junit.Assert;

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.fail;

/**
 * Executes a batch of {@link ConcurrentTask}s concurrently for testing purposes.
 */
public class ConcurrentRunner
    extends ComponentSupport
{
  List<ConcurrentTestWorker> testWorkers = new ArrayList<>();

  // A cyclic barrier ensures that the threads align after each unit of work, to avoid some completing much before
  // the others, thereby increasing contention
  private CyclicBarrier startingGun;

  // A signal to get test workers to abort if a peer finds a failure
  private final AtomicBoolean failureHasOccurred = new AtomicBoolean(false);

  private final int iterations;

  private final int terminationWaitTimeSeconds;

  /**
   * @param iterations                 How many times each task should be invoked
   * @param terminationWaitTimeSeconds How long to wait for all iterations to complete
   */
  public ConcurrentRunner(int iterations, int terminationWaitTimeSeconds) {
    this.iterations = iterations;
    this.terminationWaitTimeSeconds = terminationWaitTimeSeconds;
  }

  /**
   * Adds a task to the runner. The task is executed 'threads' number of times each iteration.
   */
  public void addTask(int threads, final ConcurrentTask task)
  {
    Preconditions.checkArgument(threads >= 1);
    for (int i = 0; i < threads; i++) {
      testWorkers.add(new ConcurrentTestWorker(testWorkers.size(), startingGun, failureHasOccurred, iterations, task));
    }
  }

  public void go() throws Exception {
    initStartingGun();

    ExecutorService service = Executors.newFixedThreadPool(testWorkers.size());

    for (Runnable r : testWorkers) {
      service.submit(r);
    }

    service.shutdown();
    service.awaitTermination(terminationWaitTimeSeconds, TimeUnit.SECONDS);

    for (ConcurrentTestWorker testWorker : testWorkers) {
      if (testWorker.experiencedFailure()) {
        Assert.fail("A test worker experienced a failure.");
      }
    }
  }

  private void initStartingGun() {
    startingGun = new CyclicBarrier(testWorkers.size(), new Runnable()
    {
      int iterationCount = 0;

      @Override
      public void run() {
        log.debug("Iteration starting: " + iterationCount++);
      }
    });
  }
}

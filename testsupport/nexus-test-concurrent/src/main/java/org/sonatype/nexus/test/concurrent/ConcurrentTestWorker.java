package org.sonatype.nexus.test.concurrent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * A helper class to wrap a {@link ConcurrentTask} and trap any exceptions that it throws. If an exception is thrown, it
 * trips a flag to force its peers to abort sooner, rather than waiting for the executor service to time out.
 */
class ConcurrentTestWorker
    extends ComponentSupport
    implements Runnable
{
  private final CyclicBarrier iterationStartSignal;

  private final AtomicBoolean failureSignal;

  private final int iterations;

  private final int workerNumber;

  private final ConcurrentTask concurrentTask;

  private final AtomicReference<Exception> exception = new AtomicReference<>();

  public ConcurrentTestWorker(final int workerNumber, final CyclicBarrier iterationStartSignal,
                              final AtomicBoolean failureSignal,
                              final int iterations,
                              final ConcurrentTask concurrentTask)
  {
    this.concurrentTask = concurrentTask;
    this.iterationStartSignal = iterationStartSignal;
    this.failureSignal = failureSignal;
    this.iterations = iterations;
    this.workerNumber = workerNumber;
  }

  @Override
  public void run() {
    try {
      for (int i = 0; i < iterations; i++) {
        if (failureSignal.get()) {
          log.info("Test worker " + workerNumber + " aborting before iteration " + i);
          break;
        }
        iterationStartSignal.await();
        concurrentTask.run();
        log.info("Test worker " + workerNumber + " completing iteration " + i);
      }
    }
    catch (Exception e) {
      failureSignal.set(true);
      // Dislodge any peers waiting at the barrier; the test has failed, so don't wait for timeout
      iterationStartSignal.reset();
      this.exception.set(e);
      log.info("Runnable threw an exception for worker " + workerNumber, e);
    }
    finally {
      log.info("Test worker " + workerNumber + " completing.");
    }
  }

  public boolean experiencedFailure() {
    return exception.get() != null;
  }

  public Exception getException() {
    return exception.get();
  }
}

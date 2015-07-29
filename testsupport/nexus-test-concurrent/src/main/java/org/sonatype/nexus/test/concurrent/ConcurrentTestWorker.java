package org.sonatype.nexus.test.concurrent;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper class to wrap a {@link ConcurrentTask} and trap any exceptions that it throws. If an exception is thrown,
 * it
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

  private final AtomicReference<Throwable> exception = new AtomicReference<>();

  private final AtomicInteger runInvocations;

  public ConcurrentTestWorker(final int workerNumber,
                              final CyclicBarrier iterationStartSignal,
                              final AtomicBoolean failureSignal,
                              final int iterations,
                              final ConcurrentTask concurrentTask,
                              final AtomicInteger runInvocations)
  {
    this.workerNumber = workerNumber;
    this.iterationStartSignal = checkNotNull(iterationStartSignal);
    this.failureSignal = checkNotNull(failureSignal);
    this.iterations = iterations;
    this.concurrentTask = checkNotNull(concurrentTask);
    this.runInvocations = checkNotNull(runInvocations);
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

        runInvocations.incrementAndGet();
        log.info("Test worker " + workerNumber + " incrementing runInvocations to " + runInvocations.get());
        concurrentTask.run();
        log.info("Test worker " + workerNumber + " completing iteration " + i);
      }
    }
    catch (Exception | AssertionError e) {
      log.info("Runnable threw an exception for worker " + workerNumber, e);
      failureSignal.set(true);
      // Dislodge any peers waiting at the barrier; the test has failed, so don't wait for timeout
      iterationStartSignal.reset();
      this.exception.set(e);
    }
    finally {
      log.info("Test worker " + workerNumber + " completing.");
    }
  }

  public boolean experiencedFailure() {
    return exception.get() != null;
  }

  public Throwable getException() {
    return exception.get();
  }
}

package org.sonatype.nexus.test.concurrent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Context shared between the {@link ConcurrentRunner} and the various {@link ConcurrentTestWorker}s.
 */
class ConcurrentTestContext
{
  private final CyclicBarrier iterationStartSignal;

  private final AtomicBoolean failureSignal;

  private final int iterations;

  private final AtomicInteger runInvocations;

  private final int iterationStartTimeoutSeconds;

  public ConcurrentTestContext(final CyclicBarrier iterationStartSignal,
                               final AtomicBoolean failureSignal,
                               final int iterations,
                               final AtomicInteger runInvocations,
                               final int iterationStartTimeoutSeconds)
  {
    this.iterationStartTimeoutSeconds = iterationStartTimeoutSeconds;
    this.iterationStartSignal = checkNotNull(iterationStartSignal);
    this.failureSignal = checkNotNull(failureSignal);
    this.iterations = iterations;
    this.runInvocations = checkNotNull(runInvocations);
  }

  public void recordRunInvocation() {
    runInvocations.incrementAndGet();
  }

  public int getIterations() {
    return iterations;
  }

  public boolean testFailing() {
    return failureSignal.get();
  }

  public void indicateFailure() {
    failureSignal.set(true);
    // Dislodge any peers waiting at the barrier; the test has failed, so don't wait for timeout
    iterationStartSignal.reset();
  }

  public void awaitIterationStart() throws InterruptedException, TimeoutException, BrokenBarrierException {
    iterationStartSignal.await(iterationStartTimeoutSeconds, TimeUnit.SECONDS);
  }
}

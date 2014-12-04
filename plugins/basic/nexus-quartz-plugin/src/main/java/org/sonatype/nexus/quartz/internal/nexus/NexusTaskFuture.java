/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.quartz.internal.nexus;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sonatype.nexus.scheduling.TaskInfo.RunState;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import org.quartz.JobKey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Nexus task {@link Future}. Is created by {@link NexusTaskJobListener} and is stick into job context.
 *
 * @since 3.0
 */
public class NexusTaskFuture<T>
    extends ComponentSupport
    implements Future<T>
{
  /**
   * Key used in job execution context to stick future in.
   */
  static final String FUTURE_KEY = NexusTaskFuture.class.getName();

  private final QuartzNexusSchedulerSPI quartzSupport;

  private final JobKey jobKey;

  private final Date startedAt;

  private final Schedule startedBy;

  private final CountDownLatch countDownLatch;

  private volatile RunState runState;

  private Exception exception;

  private T result;

  public NexusTaskFuture(final QuartzNexusSchedulerSPI quartzSupport,
                         final JobKey jobKey,
                         final Date startedAt,
                         final Schedule startedBy)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.startedAt = checkNotNull(startedAt);
    this.startedBy = checkNotNull(startedBy);
    this.countDownLatch = new CountDownLatch(1);
    this.runState = RunState.STARTING;
  }

  public void setResult(final T result, final Exception exception) {
    this.result = result;
    this.exception = exception;
    countDownLatch.countDown();
  }

  public Date getStartedAt() {
    return startedAt;
  }

  public Schedule getStartedBy() {
    return startedBy;
  }

  public RunState getRunState() {
    return runState;
  }

  public void setRunState(final RunState runState) {
    checkState(this.runState.ordinal() <= runState.ordinal(),
        "Illegal run state transition: %s -> %s", this.runState, runState);
    log.debug("NX Task {} runState transition {} -> {}", jobKey, this.runState, runState);
    this.runState = runState;
  }

  public void doCancel() {
    setRunState(RunState.CANCELED);
    setResult(null, new CancellationException("Task canceled"));
  }

  // == TaskFuture

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    if (isCancelled()) {
      return true;
    }
    doCancel();
    return quartzSupport.cancelJob(jobKey);
  }

  @Override
  public boolean isCancelled() {
    return runState == RunState.CANCELED;
  }

  @Override
  public boolean isDone() {
    return countDownLatch.getCount() == 0;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    countDownLatch.await();
    if (exception != null) {
      Throwables.propagateIfPossible(exception);
      Throwables.propagateIfInstanceOf(exception, InterruptedException.class);
      Throwables.propagateIfInstanceOf(exception, ExecutionException.class);
      throw new ExecutionException("Job failure: ", exception);
    }
    return result;
  }

  @Override
  public T get(final long timeout, final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException
  {
    countDownLatch.await(timeout, unit);
    if (exception != null) {
      Throwables.propagateIfPossible(exception);
      Throwables.propagateIfInstanceOf(exception, InterruptedException.class);
      Throwables.propagateIfInstanceOf(exception, ExecutionException.class);
      throw new ExecutionException("Job failure: ", exception);
    }
    return result;
  }
}
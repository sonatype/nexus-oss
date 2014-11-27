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
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskRemovedException;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;

import com.google.common.base.Throwables;
import org.quartz.JobKey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link TaskInfo} support class backed by Quartz.
 *
 * @since 3.0
 */
public class NexusTaskInfo<T>
    implements TaskInfo<T>
{
  /**
   * Key used in job execution context to stick task info in.
   */
  static final String TASK_INFO_KEY = NexusTaskInfo.class.getName();

  private final QuartzNexusSchedulerSPI quartzSupport;

  private final JobKey jobKey;

  private volatile CountDownLatch countDownLatch;

  private volatile NexusTaskState nexusTaskState;

  private volatile NexusTaskFuture<T> nexusTaskFuture;

  public NexusTaskInfo(final QuartzNexusSchedulerSPI quartzSupport,
                       final JobKey jobKey,
                       final NexusTaskState nexusTaskState)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.countDownLatch = nexusTaskState.getSchedule() instanceof Now ? new CountDownLatch(1) : new CountDownLatch(0);
    this.nexusTaskState = checkNotNull(nexusTaskState);
    this.nexusTaskFuture = null;
  }

  public synchronized void setNexusTaskState(final NexusTaskState nexusTaskState,
                                             final @Nullable NexusTaskFuture<T> nexusTaskFuture)
  {
    checkState(State.RUNNING != nexusTaskState.getState() || nexusTaskFuture != null, "Running task must have future");
    this.nexusTaskState = checkNotNull(nexusTaskState);
    this.nexusTaskFuture = nexusTaskFuture;
    this.countDownLatch.countDown();
  }

  @Override
  public String getId() {
    return getConfiguration().getId();
  }

  @Override
  public String getName() {
    return getConfiguration().getName();
  }

  @Override
  public String getMessage() {
    return getConfiguration().getMessage();
  }

  @Override
  public TaskConfiguration getConfiguration() {
    return nexusTaskState.getConfiguration();
  }

  @Override
  public Schedule getSchedule() {
    return nexusTaskState.getSchedule();
  }

  @Override
  public CurrentState<T> getCurrentState() {
    try {
      countDownLatch.await();
    }
    catch (InterruptedException e) {
      throw Throwables.propagate(e);
    }
    // get all 2 in consistent manner but w/o deadlock on latch.await
    synchronized (this) {
      return new CS<>(nexusTaskState, nexusTaskFuture);
    }
  }

  @Nullable
  @Override
  public LastRunState getLastRunState() {
    return nexusTaskState.getLastRunState();
  }

  @Override
  public boolean remove() {
    return quartzSupport.removeTask(jobKey.getName());
  }

  @Override
  public synchronized TaskInfo<T> runNow() throws TaskRemovedException {
    checkState(State.RUNNING != nexusTaskState.getState(), "Task already running");
    countDownLatch = new CountDownLatch(1);
    quartzSupport.runNow(jobKey);
    return this;
  }

  // ==

  static class CS<T>
      implements CurrentState<T>
  {
    private final State state;

    private final Date nextRun;

    private final NexusTaskFuture<T> future;

    public CS(final NexusTaskState nexusTaskState, final NexusTaskFuture<T> nexusTaskFuture)
    {
      this.state = nexusTaskState.getState();
      this.nextRun = nexusTaskState.getNextExecutionTime();
      this.future = nexusTaskFuture;
    }

    @Override
    public State getState() {
      return state;
    }

    @Override
    public Date getNextRun() {
      return nextRun;
    }

    @Override
    public Date getRunStarted() {
      return state == State.RUNNING ? future.getStartedAt() : null;
    }

    @Override
    public RunState getRunState() {
      return state == State.RUNNING ? future.getRunState() : null;
    }

    @Override
    public NexusTaskFuture<T> getFuture() {
      return future;
    }
  }
}
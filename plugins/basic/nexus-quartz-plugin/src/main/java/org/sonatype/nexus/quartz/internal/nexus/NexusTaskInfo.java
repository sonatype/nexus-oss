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

import javax.annotation.Nullable;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskRemovedException;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import org.quartz.JobKey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link TaskInfo} support class backed by Quartz.
 *
 * Note about {@link NexusTaskFuture}: when this class has future (is not null), the task is meant to be started
 * (either by schedule or by "runNow"). When this class has no future, that means that task is in WAITING or DONE
 * states.
 *
 * @since 3.0
 */
public class NexusTaskInfo<T>
    extends ComponentSupport
    implements TaskInfo<T>
{
  /**
   * Key used in job execution context to stick task info in.
   */
  static final String TASK_INFO_KEY = NexusTaskInfo.class.getName();

  private final QuartzNexusSchedulerSPI quartzSupport;

  private final JobKey jobKey;

  private volatile State state;

  private volatile NexusTaskState nexusTaskState;

  private volatile NexusTaskFuture<T> nexusTaskFuture;

  private volatile boolean removed;

  public NexusTaskInfo(final QuartzNexusSchedulerSPI quartzSupport,
                       final JobKey jobKey,
                       final NexusTaskState nexusTaskState,
                       final @Nullable NexusTaskFuture<T> nexusTaskFuture)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.state = nexusTaskFuture != null ? State.RUNNING : State.WAITING;
    this.nexusTaskState = nexusTaskState;
    this.nexusTaskFuture = nexusTaskFuture;
    this.removed = false;
  }

  public synchronized void setNexusTaskState(final State state,
                                             final NexusTaskState nexusTaskState,
                                             final @Nullable NexusTaskFuture<T> nexusTaskFuture)
  {
    checkNotNull(state);
    checkNotNull(nexusTaskState);
    checkState(State.RUNNING != state || nexusTaskFuture != null, "Running task must have future");
    log.debug("NX Task transition {} -> {}", this.state, state);
    this.state = state;
    this.nexusTaskState = nexusTaskState;
    this.nexusTaskFuture = nexusTaskFuture;
  }

  @Nullable
  public NexusTaskFuture<T> getNexusTaskFuture() {
    return nexusTaskFuture;
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
  public synchronized TaskConfiguration getConfiguration() {
    return nexusTaskState.getConfiguration();
  }

  @Override
  public synchronized Schedule getSchedule() {
    return nexusTaskState.getSchedule();
  }

  @Override
  public synchronized CurrentState<T> getCurrentState() {
    return new CS<>(state, nexusTaskState, nexusTaskFuture);
  }

  @Nullable
  @Override
  public synchronized LastRunState getLastRunState() {
    return nexusTaskState.getLastRunState();
  }

  @Override
  public synchronized boolean remove() {
    if (removed || state == State.DONE) {
      // already removed
      return false;
    }
    if (nexusTaskFuture != null) {
      nexusTaskFuture.cancel(true);
    }
    if (!NexusTaskState.hasLastRunState(nexusTaskState.getConfiguration().getMap())) {
      // if no last state (removed even before 1st run), add one noting it got removed/canceled
      // if was running and is cancelable, the task will itself set a proper ending state
      NexusTaskState.setLastRunState(nexusTaskState.getConfiguration().getMap(), EndState.CANCELED, new Date(), 0L);
    }
    removed = true;
    log.info("NX Task remove: {}", nexusTaskState);
    return quartzSupport.removeTask(jobKey);
  }

  @Override
  public synchronized TaskInfo<T> runNow() throws TaskRemovedException {
    checkState(State.RUNNING != state, "Task already running");
    checkState(!removed, "Task already removed");
    log.info("NX Task runNow: {}", nexusTaskState);
    setNexusTaskState(
        State.RUNNING,
        nexusTaskState,
        new NexusTaskFuture<T>(
            quartzSupport,
            jobKey,
            new Date(),
            new Now()
        )
    );
    try {
      quartzSupport.runNow(jobKey);
    }
    catch (Exception e) {
      Throwables.propagateIfInstanceOf(e, TaskRemovedException.class);
      throw Throwables.propagate(e);
    }
    return this;
  }

  // ==

  static class CS<T>
      implements CurrentState<T>
  {
    private final State state;

    private final Date nextRun;

    private final NexusTaskFuture<T> future;

    public CS(final State state, final NexusTaskState nexusTaskState, final NexusTaskFuture<T> nexusTaskFuture)
    {
      this.state = state;
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
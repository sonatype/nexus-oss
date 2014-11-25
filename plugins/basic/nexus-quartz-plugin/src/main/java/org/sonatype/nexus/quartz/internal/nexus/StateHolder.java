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
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState;
import org.sonatype.nexus.scheduling.TaskInfo.EndState;
import org.sonatype.nexus.scheduling.TaskInfo.LastRunState;
import org.sonatype.nexus.scheduling.TaskInfo.RunState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.schedule.Schedule;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Holder for state of a task. Pushed by {@link NexusTaskJobListener}.
 *
 * @since 3.0
 */
public class StateHolder<T>
{
  private final TaskConfiguration taskConfiguration;

  private final Schedule schedule;

  private final CurrentState<T> currentState;

  private final LastRunState lastRunState;

  public StateHolder(final @Nullable NexusTaskFuture<T> nexusTaskFuture,
                     final boolean running,
                     final TaskConfiguration taskConfiguration,
                     final Schedule schedule,
                     final @Nullable Date nextExecutionTime)
  {
    this.taskConfiguration = checkNotNull(taskConfiguration);
    this.schedule = checkNotNull(schedule);
    this.currentState = extractCurrentState(running, nexusTaskFuture, nextExecutionTime);
    this.lastRunState = extractLastRunState(taskConfiguration);
  }

  public TaskConfiguration getConfiguration() {
    return taskConfiguration;
  }

  public Schedule getSchedule() {
    return schedule;
  }

  public CurrentState<T> getCurrentState() {
    return currentState;
  }

  @Nullable
  public LastRunState getLastRunState() {
    return lastRunState;
  }

  // ==

  private CurrentState<T> extractCurrentState(final boolean running, final NexusTaskFuture<T> future,
                                              final Date nextExecutionTime)
  {
    checkState(!running || future != null, "Running task must have future");
    State state;
    Date nextRun;
    Date runStarted;
    RunState runState;
    if (running) {
      state = State.RUNNING;
      nextRun = nextExecutionTime;
      runStarted = future.getStartedAt();
      runState = future.getRunState();
    }
    else {
      state = nextExecutionTime == null ? State.DONE : State.WAITING;
      nextRun = nextExecutionTime;
      runStarted = null;
      runState = null;
    }
    return new CS<>(state, nextRun, runStarted, runState, future);
  }

  private LastRunState extractLastRunState(final TaskConfiguration taskConfiguration) {
    if (taskConfiguration.getMap().containsKey("lastRunState.endState")) {
      final String endStateString = taskConfiguration.getString("lastRunState.endState");
      final long runStarted = taskConfiguration.getLong("lastRunState.runStarted", -1);
      final long runDuration = taskConfiguration.getLong("lastRunState.runDuration", -1);
      return new LS(EndState.valueOf(endStateString), new Date(runStarted), runDuration);
    }
    return null;
  }

  // ==

  static class CS<T>
      implements CurrentState<T>
  {
    private final State state;

    private final Date nextRun;

    private final Date runStarted;

    private final RunState runState;

    private final Future<T> future;

    public CS(final State state, final Date nextRun, final Date runStarted, final RunState runState,
              final Future<T> future)
    {
      this.state = state;
      this.nextRun = nextRun;
      this.runStarted = runStarted;
      this.runState = runState;
      this.future = future;
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
      return runStarted;
    }

    @Override
    public RunState getRunState() {
      return runState;
    }

    @Override
    public Future<T> getFuture() {
      return future;
    }
  }

  static class LS
      implements LastRunState
  {
    private final EndState endState;

    private final Date runStarted;

    private final long runDuration;

    public LS(final EndState endState, final Date runStarted, final long runDuration) {
      this.endState = endState;
      this.runStarted = runStarted;
      this.runDuration = runDuration;
    }

    @Override
    public EndState getEndState() {
      return endState;
    }

    @Override
    public Date getRunStarted() {
      return runStarted;
    }

    @Override
    public long getRunDuration() {
      return runDuration;
    }
  }
}
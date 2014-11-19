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
package org.sonatype.nexus.scheduling;

import java.util.Date;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import org.sonatype.nexus.scheduling.schedule.Schedule;

/**
 * The class holding information about tasks. This is the "handle" of a scheduled task.
 *
 * @since 3.0
 */
public interface TaskInfo<T>
{
  /**
   * Returns a unique ID of the task instance. Shorthand method for {@link #getConfiguration()#getId()}
   */
  String getId();

  /**
   * Returns a name of the task instance. Shorthand method for {@link #getConfiguration()#getName()}
   */
  String getName();

  /**
   * Returns a message of the task instance. Shorthand method for {@link #getConfiguration()#getMessage()}
   */
  String getMessage();

  /**
   * Returns a COPY of the task configuration map. Modifications to this configuration are possible, but does not
   * affect currently executing task, nor is being persisted. Generally, this configuration is only for inspection,
   * or, to be used to re-schedule existing task with change configuration.
   */
  TaskConfiguration getConfiguration();

  /**
   * Returns the task's schedule.
   */
  Schedule getSchedule();

  /**
   * Executes the scheduled task now, unrelated to it's actual schedule.
   */
  void runNow();

  // ==

  /**
   * Task instance might be waiting (to be run, either by schedule or manually), or might be running.
   *
   * WAITING -> RUNNING
   * RUNNING -> WAITING
   */
  enum State
  {
    WAITING, RUNNING
  }

  /**
   * Running task instance might be running okay, being blocked (by other tasks), or might be canceled but the
   * cancellation is not yet detected, or some cleanup is happening.
   *
   * BLOCKED->RUNNING
   * RUNNING->CANCELED
   */
  enum RunState
  {
    BLOCKED, RUNNING, CANCELED
  }

  interface CurrentState<T>
  {
    /**
     * Returns the state of task, never {@code null}.
     */
    State getState();

    /**
     * Returns the date of next run, if applicable, or {@code null}.
     */
    @Nullable
    Date getNextRun();

    /**
     * If task is running, returns it's run state, otherwise {@code null}.
     */
    @Nullable
    Date getRunStarted();

    /**
     * If task is running, returns it's run state, otherwise {@code null}.
     */
    @Nullable
    RunState getRunState();

    /**
     * If task is running, returns it's future, otherwise {@code null}.
     */
    @Nullable
    Future<T> getFuture();
  }

  enum EndState
  {
    OK, FAILED, CANCELED
  }

  interface LastRunState
  {
    /**
     * Returns the last end state.
     */
    EndState getEndState();

    /**
     * Returns the date of last run start.
     */
    Date getRunStarted();

    /**
     * Returns the last run duration.
     */
    long getRunDuration();
  }

  // ==

  /**
   * Returns the task current state, never {@code null}.
   *
   * @throws IllegalStateException if task with this ID has been removed from scheduler.
   */
  CurrentState<T> getCurrentState() throws IllegalStateException;

  /**
   * Returns the task last run state, if there was any, otherwise {@code null}.
   *
   * @throws IllegalStateException if task with this ID has been removed from scheduler.
   */
  @Nullable
  LastRunState getLastRunState() throws IllegalStateException;
}

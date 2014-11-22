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
 * The class holding information about task at the moment the instance of task info was created.
 * This is the "handle" of a scheduled task. The handle might become "stale"
 * if the task this instance is handle of is removed from scheduler (ie. by some other thread). In that case,
 * some of the methods will throw {@link TaskRemovedException} on invocation to signal that state. For task entering
 * {@link State#DONE}, this class will behave a bit differently: they will never throw {@link TaskRemovedException},
 * and upon they are done, the task info will cache task configuration, schedule, current and last run state forever.
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
   * Returns a COPY of the task configuration map from the moment this instance was created. Modifications to this
   * configuration are possible, but does not affect currently executing task, nor is being persisted. Generally, this
   * configuration is only for inspection, or, to be used to re-schedule existing task with change configuration. If
   * in the meanwhile, task finishes, and modifies configuration, it is NOT reflected in configuration returned by
   * this method, you need to "reload" task info using {@link #refresh()} method.
   */
  TaskConfiguration getConfiguration();

  /**
   * Returns the task's schedule.
   */
  Schedule getSchedule();

  // ==

  /**
   * Task instance might be waiting (to be run, either by schedule or manually), or might be running, or might be
   * done (will never run again, is "done"). The "done" state is ending state for task, it will according to it's
   * {@link Schedule} not execute anymore.
   *
   * WAITING -> RUNNING
   * RUNNING -> WAITING
   * RUNNING -> DONE
   *
   * @see {@link #refresh()} method.
   */
  enum State
  {
    WAITING, RUNNING, DONE
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
     * If task is in states {@link State#RUNNING} or {@link State#DONE}, returns it's future, otherwise {@code null}.
     * In case of {@link State#DONE} the future is done too.
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
   */
  CurrentState<T> getCurrentState();

  /**
   * Returns the task last run state, if there was any, otherwise {@code null}.
   */
  @Nullable
  LastRunState getLastRunState();

  // ==

  /**
   * Removes (with canceling it if it runs) the task. Returns {@code true} if removal succeeded (task was found and was
   * removed), otherwise {@code false}.
   */
  boolean remove();

  /**
   * Executes the scheduled task now, unrelated to it's actual schedule.
   *
   * @throws TaskRemovedException if task with this ID has been removed from scheduler.
   */
  void runNow() throws TaskRemovedException;

  /**
   * Returns a fresh task info (with actualized values). If the task got removed from scheduler (by some other thread),
   * an {@link TaskRemovedException} will be thrown. If this instance was already in ending {@link State#DONE} state,
   * this same instance is returned.
   *
   * @throws TaskRemovedException if task with this ID has been removed from scheduler.
   */
  TaskInfo<T> refresh() throws TaskRemovedException;
}

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

import java.util.concurrent.CountDownLatch;

import javax.annotation.Nullable;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskRemovedException;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;

import com.google.common.base.Throwables;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

import static com.google.common.base.Preconditions.checkNotNull;

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

  private final CountDownLatch countDownLatch;

  private volatile StateHolder<T> stateHolder;

  public NexusTaskInfo(final QuartzNexusSchedulerSPI quartzSupport,
                       final JobKey jobKey,
                       final StateHolder<T> stateHolder)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.countDownLatch = new CountDownLatch(1);
    this.stateHolder = checkNotNull(stateHolder);
  }

  public StateHolder<T> getStateHolder() {
    return stateHolder;
  }

  public void setStateHolder(final StateHolder<T> stateHolder) {
    this.stateHolder = checkNotNull(stateHolder);
    countDownLatch.countDown();
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
    return getStateHolder().getConfiguration();
  }

  @Override
  public Schedule getSchedule() {
    return getStateHolder().getSchedule();
  }

  @Override
  public CurrentState<T> getCurrentState() {
    if (getSchedule() instanceof Now) {
      // TODO: this makes sense for "bg jobs" only! In other cases this might cause unacceptable long blocked threads!
      // TODO: for bg jobs caller usually submits it and wants the result of it, kinda "thread pool" use of scheduler
      try {
        countDownLatch.await();
      }
      catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
    }
    return getStateHolder().getCurrentState();
  }

  @Nullable
  @Override
  public LastRunState getLastRunState() {
    return getStateHolder().getLastRunState();
  }

  @Override
  public boolean remove() {
    return quartzSupport.removeTask(jobKey.getName());
  }

  @Override
  public void runNow() throws TaskRemovedException {
    quartzSupport.runNow(jobKey);
  }

  @Override
  public TaskInfo<T> refresh() throws TaskRemovedException {
    if (State.DONE != getCurrentState().getState()) {
      try {
        setStateHolder((StateHolder<T>) quartzSupport.taskByKey(jobKey).getStateHolder());
      }
      catch (SchedulerException e) {
        throw Throwables.propagate(e);
      }
      catch (IllegalStateException e) {
        throw new TaskRemovedException(jobKey.getName(), e);
      }
    }
    return this;
  }
}
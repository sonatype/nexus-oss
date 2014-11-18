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
package org.sonatype.nexus.scheduling.internal;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.nexus.scheduling.spi.NexusTaskExecutorSPI;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.eclipse.sisu.Priority;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Simple SPI using ThreadPoolExecutor that supports only simple execution of background tasks, but not scheduling.
 *
 * @since 3.0
 */
@Singleton
@Named
// TODO: I want this implementation to be last, see DefaultNexusTaskScheduler#getScheduler method
@Priority(-1000) // be last, sorta fallback? (and used in tests)
public class ThreadPoolNexusSchedulerSPI
    extends ComponentSupport
    implements NexusTaskExecutorSPI
{
  private final NexusTaskFactory nexusTaskFactory;

  private final ThreadPoolExecutor executorService;

  @Inject
  public ThreadPoolNexusSchedulerSPI(final NexusTaskFactory nexusTaskFactory)
  {
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
    this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
  }

  private static class TestTaskInfo<T>
      implements TaskInfo<T>
  {
    private final TaskConfiguration taskConfiguration;

    private final Schedule schedule;

    private final Future<T> future;

    private final Date runStarted;

    public TestTaskInfo(final TaskConfiguration taskConfiguration, final Schedule schedule, final Future<T> future) {
      this.taskConfiguration = taskConfiguration;
      this.schedule = schedule;
      this.future = future;
      this.runStarted = new Date();
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
    public TaskConfiguration getConfiguration() {
      return taskConfiguration;
    }

    @Override
    public Schedule getSchedule() {
      return schedule;
    }

    @Override
    public String getMessage() {
      return getConfiguration().getMessage();
    }

    @Override
    public void runNow() {
      throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CurrentState getCurrentState() {
      checkState(!future.isDone());
      return new CurrentState()
      {
        @Override
        public State getState() {
          return State.RUNNING;
        }

        @Nullable
        @Override
        public Date getNextRun() {
          return null;
        }

        @Nullable
        @Override
        public Date getRunStarted() {
          return runStarted;
        }

        @Nullable
        @Override
        public RunState getRunState() {
          return RunState.RUNNING;
        }

        @Nullable
        @Override
        public Future getFuture() {
          return future;
        }
      };
    }

    @Nullable
    @Override
    public LastRunState getLastRunState() {
      checkState(!future.isDone());
      return null;
    }
  }

  @Override
  public <T> TaskInfo<T> getTaskById(final String id) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public List<TaskInfo<?>> listsTasks() {
    executorService.getActiveCount();
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public <T> TaskInfo<T> scheduleTask(final TaskConfiguration taskConfiguration, final Schedule schedule) {
    checkNotNull(taskConfiguration);
    checkArgument(schedule instanceof Now);
    final Task<T> task = nexusTaskFactory.createTaskInstance(taskConfiguration);
    return new TestTaskInfo<>(taskConfiguration, schedule, executorService.submit(task));
  }

  @Override
  public boolean removeTask(final String id) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public int getRunnintTaskCount() {
    return executorService.getActiveCount();
  }
}

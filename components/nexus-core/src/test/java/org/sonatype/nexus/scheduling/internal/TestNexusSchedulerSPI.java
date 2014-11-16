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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple SPI using ThreadPool for tests. It supports only the submit method aka "run now bg task" used in UTs.
 */
@Singleton
@Named
public class TestNexusSchedulerSPI
    extends ComponentSupport
    implements NexusTaskExecutorSPI
{
  private final NexusTaskFactory nexusTaskFactory;

  private final ThreadPoolExecutor executorService;

  @Inject
  public TestNexusSchedulerSPI(final NexusTaskFactory nexusTaskFactory)
  {
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
    this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
  }

  private static class TestTaskInfo<T>
      implements TaskInfo<T>
  {
    private final TaskConfiguration taskConfiguration;

    private final Future<T> future;

    public TestTaskInfo(final TaskConfiguration taskConfiguration, final Future<T> future) {
      this.taskConfiguration = taskConfiguration;
      this.future = future;
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
      return null;
    }

    @Override
    public String getMessage() {
      return null;
    }

    @Override
    public void runNow() {
      throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public CurrentState getCurrentState() {
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
          return new Date();
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
      return null;
    }
  }

  @Override
  public <T> TaskInfo<T> getTaskById(final String id) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public List<TaskInfo<?>> listsTasks() {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public <T> TaskInfo<T> scheduleTask(final TaskConfiguration taskConfiguration, final Schedule schedule) {
    checkArgument(schedule instanceof Now);
    final Task<T> task = nexusTaskFactory.createTaskInstance(taskConfiguration);
    return new TestTaskInfo<>(taskConfiguration, executorService.submit(task));
  }

  @Override
  public boolean removeTask(final String id) {
    throw new UnsupportedOperationException("not implemented");
  }

  public int getRunningTaskCount() {
    return executorService.getActiveCount();
  }

  public void killAll() {
    executorService.shutdownNow();
  }
}

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

import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.quartz.JobSupport;
import org.sonatype.nexus.scheduling.Cancelable;
import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.RunState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.TaskInterruptedException;
import org.sonatype.nexus.scheduling.events.NexusTaskEventCanceled;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A {#link JobSupport} wrapping NX Task that is also {@link InterruptableJob} (but actual interruption ability depends
 * on underlying NX Task).
 *
 * @since 3.0
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Named
public class NexusTaskJobSupport<T>
    extends JobSupport
    implements InterruptableJob
{
  private static class OtherRunningTasks<T>
      implements Predicate<TaskInfo<?>>
  {
    private final Task<T> me;

    public OtherRunningTasks(final Task<T> me) {
      this.me = me;
    }

    @Override
    public boolean apply(final TaskInfo<?> taskInfo) {
      return !me.getId().equals(taskInfo.getId())
          && State.RUNNING == taskInfo.getCurrentState().getState();
    }
  }

  private final EventBus eventBus;

  private final Provider<QuartzNexusSchedulerSPI> quartzNexusSchedulerSPIProvider;

  private final NexusTaskFactory nexusTaskFactory;

  private Task<T> nexusTask;

  @Inject
  public NexusTaskJobSupport(final EventBus eventBus,
                             final Provider<QuartzNexusSchedulerSPI> quartzNexusSchedulerSPIProvider,
                             final NexusTaskFactory nexusTaskFactory)
  {
    this.eventBus = checkNotNull(eventBus);
    this.quartzNexusSchedulerSPIProvider = checkNotNull(quartzNexusSchedulerSPIProvider);
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
  }

  @Override
  public void execute() throws Exception {
    Exception ex = null;
    try {
      final TaskConfiguration taskConfiguration = toTaskConfiguration(context.getJobDetail().getJobDataMap());
      final NexusTaskFuture<T> future = (NexusTaskFuture) context.get(NexusTaskFuture.FUTURE_KEY);
      nexusTask = nexusTaskFactory.createTaskInstance(taskConfiguration);
      try {
        mayBlock(nexusTask, future);
        if (!future.isCancelled()) {
          future.setRunState(RunState.RUNNING);
          nexusTask.getConfiguration().setMessage(nexusTask.getMessage());
          final T result = nexusTask.call();
          context.setResult(result);
          // put back any state task modified to have it persisted
          context.getJobDetail().getJobDataMap().putAll(nexusTask.getConfiguration().getMap());
        }
      }
      catch (TaskInterruptedException e) {
        log.warn("Task canceled: {}:{}", taskConfiguration.getTypeId(), taskConfiguration.getId(), e);
        getTaskInfo().getNexusTaskFuture().doCancel();
      }
      catch (Exception e) {
        log.warn("Task execution failure: {}:{}", taskConfiguration.getTypeId(), taskConfiguration.getId(), e);
        ex = e;
      }
    }
    catch (Exception e) {
      log.warn("Task instantiation failure: {}", context.getJobDetail().getKey(), e);
      ex = e;
    }
    if (ex != null) {
      throw ex;
    }
  }

  private void mayBlock(final Task<T> nexusTask, final NexusTaskFuture<T> future) {
    // filter for running tasks, to be reused
    final OtherRunningTasks<T> otherRunningTasks = new OtherRunningTasks<>(nexusTask);
    List<TaskInfo<?>> blockedBy;
    do {
      blockedBy = nexusTask.isBlockedBy(Lists.newArrayList(Iterables.filter(
          quartzNexusSchedulerSPIProvider.get().listsTasks(), otherRunningTasks)));
      // wait for them all
      if (blockedBy != null && !blockedBy.isEmpty()) {
        try {
          // will ISEx if canceled!
          future.setRunState(RunState.BLOCKED);
          for (TaskInfo<?> taskInfo : blockedBy) {
            try {
              taskInfo.getCurrentState().getFuture().get();
            }
            catch (Exception e) {
              // we don't care if other task failed or not, it will report itself
            }
          }
        }
        catch (IllegalStateException e) {
          // task got canceled, setRunState ISEx
          break;
        }
      }
    }
    while (!blockedBy.isEmpty());
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    if (nexusTask == null) {
      return;
    }
    if (nexusTask instanceof Cancelable) {
      ((Cancelable) nexusTask).cancel();
      eventBus.post(new NexusTaskEventCanceled<>(getTaskInfo()));
    }
    else {
      throw new UnableToInterruptJobException("Task " + nexusTask + " not Cancellable");
    }
  }

  private NexusTaskInfo<T> getTaskInfo() {
    final NexusTaskInfo<T> taskInfo = (NexusTaskInfo) context.get(NexusTaskInfo.TASK_INFO_KEY);
    checkState(taskInfo != null);
    return taskInfo;
  }

  /**
   * Creates {@link TaskConfiguration} out of provided {@link JobDataMap}, by copying only those values that
   * are Strings (as task configuration is {@code Map<String, String>} while job data map values are Objects.
   */
  public static TaskConfiguration toTaskConfiguration(final JobDataMap jobDataMap) {
    final TaskConfiguration taskConfiguration = new TaskConfiguration();
    for (Entry<String, Object> entry : jobDataMap.entrySet()) {
      if (entry.getValue() instanceof String) {
        taskConfiguration.getMap().put(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }
    return taskConfiguration;
  }
}
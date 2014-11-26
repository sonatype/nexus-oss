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

import org.sonatype.nexus.scheduling.TaskInfo.EndState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.listeners.JobListenerSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.quartz.internal.nexus.NexusTaskJobSupport.toTaskConfiguration;

/**
 * A {#link JobListenerSupport} that provides NX Task integration by creating future when task starts, recording
 * execution results. Each NX Task wrapping job has one listener. Since NX Job wrapping tasks cannot concurrently
 * execute ("unique per jobKey", basically per NX Task "instance"), this listener may be stateful, and maintain
 * the task info in simple way.
 *
 * @since 3.0
 */
public class NexusTaskJobListener<T>
    extends JobListenerSupport
{
  private final QuartzNexusSchedulerSPI quartzSupport;

  private final JobKey jobKey;

  private final NexusScheduleConverter nexusScheduleConverter;

  private final NexusTaskInfo<T> nexusTaskInfo;

  public NexusTaskJobListener(final QuartzNexusSchedulerSPI quartzSupport,
                              final JobKey jobKey,
                              final NexusScheduleConverter nexusScheduleConverter,
                              final StateHolder<T> initialState)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.nexusScheduleConverter = checkNotNull(nexusScheduleConverter);
    this.nexusTaskInfo = new NexusTaskInfo<>(
        quartzSupport,
        jobKey,
        initialState
    );
  }

  public NexusTaskInfo<T> getNexusTaskInfo() {
    return nexusTaskInfo;
  }

  // == JobListener

  @Override
  public void jobToBeExecuted(final JobExecutionContext context) {
    final NexusTaskFuture<T> future = new NexusTaskFuture<>(quartzSupport, jobKey, context.getFireTime());
    nexusTaskInfo.setStateHolder(
        new StateHolder<>(
            future,
            State.RUNNING,
            toTaskConfiguration(context.getJobDetail().getJobDataMap()),
            nexusScheduleConverter.toSchedule(context.getTrigger()),
            context.getTrigger().getNextFireTime()
        )
    );
    context.put(NexusTaskFuture.FUTURE_KEY, future);
    context.put(NexusTaskInfo.TASK_INFO_KEY, nexusTaskInfo);
  }

  @Override
  public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
    final NexusTaskFuture<T> future = (NexusTaskFuture<T>) context.get(NexusTaskFuture.FUTURE_KEY);
    // TODO: done jobs will be GCed
    /*
    try {
      // Job is done
      if (context.getTrigger().getNextFireTime() == null) {
        context.getScheduler().deleteJob(jobKey);
      }
    }
    catch (SchedulerException e) {
      // mute
    }
    */
    final EndState endState;
    if (jobException != null) {
      endState = EndState.FAILED;
    }
    else if (future.isCancelled()) {
      endState = EndState.CANCELED;
    }
    else {
      endState = EndState.OK;
    }
    final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    jobDataMap.put("lastRunState.endState", endState.name());
    jobDataMap.putAsString("lastRunState.runStarted", future.getStartedAt().getTime());
    jobDataMap.putAsString("lastRunState.runDuration", System.currentTimeMillis() - future.getStartedAt().getTime());

    final Schedule schedule = nexusScheduleConverter.toSchedule(context.getTrigger());
    // TODO: better "done" detection: ie. "now" scheduled task vs non-now but manually run task?
    nexusTaskInfo.setStateHolder(
        new StateHolder<>(
            future,
            schedule instanceof Now ? State.DONE : State.WAITING,
            toTaskConfiguration(jobDataMap),
            schedule,
            context.getTrigger().getNextFireTime()
        )
    );

    future.setResult((T) context.getResult(), jobException);
  }

  @Override
  public String getName() {
    return listenerName(jobKey);
  }

  // ==

  public static String listenerName(final JobKey jobKey) {
    return NexusTaskJobListener.class.getName() + ":" + jobKey.toString();
  }
}
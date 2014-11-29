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
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.listeners.JobListenerSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.quartz.TriggerKey.triggerKey;
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
  private final EventBus eventBus;

  private final QuartzNexusSchedulerSPI quartzSupport;

  private final JobKey jobKey;

  private final NexusScheduleConverter nexusScheduleConverter;

  private final NexusTaskInfo<T> nexusTaskInfo;

  public NexusTaskJobListener(final EventBus eventBus,
                              final QuartzNexusSchedulerSPI quartzSupport,
                              final JobKey jobKey,
                              final NexusScheduleConverter nexusScheduleConverter,
                              final NexusTaskState initialState)
  {
    this.eventBus = checkNotNull(eventBus);
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

  /**
   * Returns the trigger associated with NX Task wrapping job. The trigger executing this Job does NOT have to be
   * THAT trigger, think about "runNow"! So, this method returns the associated trigger, while the trigger in
   * context might be something completely different. If not found, returns {@code null}.
   */
  private Trigger getJobTrigger(final JobExecutionContext context) {
    try {
      final Trigger trigger = context.getScheduler().getTrigger(triggerKey(jobKey.getName(), jobKey.getGroup()));
      checkState(trigger != null, "NX Task job %s not having a trigger", jobKey);
      return trigger;
    }
    catch (SchedulerException e) {
      return null;
    }
  }

  @Override
  public void jobToBeExecuted(final JobExecutionContext context) {
    final Trigger jobTrigger = getJobTrigger(context);
    final NexusTaskFuture<T> future = new NexusTaskFuture<>(quartzSupport, jobKey, context.getFireTime(),
        nexusScheduleConverter.toSchedule(context.getTrigger()));
    nexusTaskInfo.setNexusTaskState(
        new NexusTaskState(
            State.RUNNING,
            toTaskConfiguration(context.getJobDetail().getJobDataMap()),
            nexusScheduleConverter.toSchedule(jobTrigger),
            jobTrigger.getNextFireTime()
        ),
        future
    );
    context.put(NexusTaskFuture.FUTURE_KEY, future);
    context.put(NexusTaskInfo.TASK_INFO_KEY, nexusTaskInfo);
    eventBus.post(new NexusTaskEventStarted<>(nexusTaskInfo));
  }

  @Override
  public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
    final NexusTaskFuture<T> future = (NexusTaskFuture<T>) context.get(NexusTaskFuture.FUTURE_KEY);
    final EndState endState;
    if (future.isCancelled()) {
      endState = EndState.CANCELED;
    }
    else if (jobException != null) {
      endState = EndState.FAILED;
    }
    else {
      endState = EndState.OK;
    }
    final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    NexusTaskState.setLastRunState(
        jobDataMap,
        endState,
        future.getStartedAt(),
        System.currentTimeMillis() - future.getStartedAt().getTime());

    // might be null: job Removed
    Trigger jobTrigger = getJobTrigger(context);
    // must never be null, so use this or that
    Trigger currentTrigger = jobTrigger != null ? jobTrigger : context.getTrigger();

    // actual schedule
    final Schedule jobSchedule = nexusScheduleConverter.toSchedule(currentTrigger);
    // state: if not removed and will fire again: WAITING, otherwise DONE
    final State state = jobTrigger != null && jobTrigger.getNextFireTime() != null ? State.WAITING : State.DONE;
    // update task state, w/ respect to future: if DONE keep future, if WAITING drop it
    nexusTaskInfo.setNexusTaskState(
        new NexusTaskState(
            state,
            toTaskConfiguration(jobDataMap),
            jobSchedule,
            jobTrigger.getNextFireTime()
        ),
        State.DONE == state ? future : null
    );

    // DONE tasks (or those already removed) should be cleaned up, as they might reschedule themselve
    if (State.DONE == state) {
      try {
        quartzSupport.removeTask(jobKey);
      }
      catch (SchedulerException e) {
        // mute
      }
    }

    // unwrap the QZ wrapped exception and set future result
    final Exception failure =
        jobException != null && jobException.getCause() instanceof Exception ?
            (Exception) jobException.getCause() : jobException;
    future.setResult(
        (T) context.getResult(),
        failure
    );

    // fire events
    switch (endState) {
      case OK:
        eventBus.post(new NexusTaskEventStoppedDone<>(nexusTaskInfo));
        break;
      case FAILED:
        eventBus.post(new NexusTaskEventStoppedFailed<>(nexusTaskInfo, failure));
        break;
      case CANCELED:
        eventBus.post(new NexusTaskEventStoppedCanceled<>(nexusTaskInfo));
        break;
    }
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
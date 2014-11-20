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
import java.util.List;
import java.util.concurrent.Future;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.schedule.Schedule;

import com.google.common.base.Throwables;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.sonatype.nexus.quartz.internal.nexus.NexusTaskJobSupport.toTaskConfiguration;

/**
 * {@link TaskInfo} support class backed by Quartz. This class queries actual state on method invocations, so keeping
 * reference to it for longer period is okay.
 *
 * @since 3.0
 */
public class NexusTaskInfo<T>
    implements TaskInfo<T>
{
  private final QuartzSupport quartzSupport;

  private final JobKey jobKey;

  private final NexusScheduleConverter nexusScheduleConverter;

  public NexusTaskInfo(final QuartzSupport quartzSupport,
                       final JobKey jobKey,
                       final NexusScheduleConverter nexusScheduleConverter)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.nexusScheduleConverter = checkNotNull(nexusScheduleConverter);
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
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(QuartzSupport.class.getClassLoader());
    try {
      final JobDetail jobDetail = quartzSupport.getScheduler().getJobDetail(jobKey);
      checkState(jobDetail != null, "Job with key %s not exists!", jobKey);
      return toTaskConfiguration(jobDetail.getJobDataMap());
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  @Override
  public Schedule getSchedule() {
    try {
      return nexusScheduleConverter.toSchedule(getTrigger());
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void runNow() {
    try {
      quartzSupport.getScheduler().triggerJob(jobKey);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public CurrentState<T> getCurrentState() {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(QuartzSupport.class.getClassLoader());
    try {
      State state = null;
      Date nextRun = null;
      Date runStarted = null;
      RunState runState = null;
      NexusTaskFuture<T> future = null;
      final JobDetail jobDetail = quartzSupport.getScheduler().getJobDetail(jobKey);
      checkState(jobDetail != null, "Job with key %s not exists!", jobKey);
      final List<JobExecutionContext> currentlyExecuting = quartzSupport.getScheduler().getCurrentlyExecutingJobs();
      for (JobExecutionContext ctx : currentlyExecuting) {
        if (jobKey.equals(ctx.getJobDetail().getKey())) {
          state = State.RUNNING;
          nextRun = getTrigger().getNextFireTime();
          runStarted = ctx.getFireTime();
          runState = RunState.RUNNING; // TODO: ? blocked? canceled?
          future = (NexusTaskFuture<T>) ctx.get(NexusTaskFuture.FUTURE_KEY);
          break;
        }
      }
      if (state == null) {
        state = State.WAITING;
        nextRun = getTrigger().getNextFireTime();
        runStarted = null;
        runState = null;
      }

      return new CS(state, nextRun, runStarted, runState, future);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  @Override
  public LastRunState getLastRunState() {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(QuartzSupport.class.getClassLoader());
    try {
      final JobDetail jobDetail = quartzSupport.getScheduler().getJobDetail(jobKey);
      checkState(jobDetail != null, "Job with key %s not exists!", jobKey);
      final JobDataMap jobDataMap = jobDetail.getJobDataMap();
      if (!jobDataMap.containsKey("lastRunState.endState")) {
        return null;
      }
      final String endStateString = jobDataMap.getString("lastRunState.endState");
      final long runStarted = jobDataMap.getLongFromString("lastRunState.runStarted");
      final long runDuration = jobDataMap.getLongFromString("lastRunState.runDuration");
      return new LS(EndState.valueOf(endStateString), new Date(runStarted), runDuration);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  // ==

  /**
   * Gets and ensures trigger for this NX job exists.
   */
  private Trigger getTrigger() throws SchedulerException {
    // jobs a triggers are kept on same combinatorial keys
    final Trigger trigger = quartzSupport.getScheduler().getTrigger(
        TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
    checkState(trigger != null, "Bug: no trigger for NX job %s", jobKey);
    return trigger;
  }

  // ==

  class CS
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

  class LS
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
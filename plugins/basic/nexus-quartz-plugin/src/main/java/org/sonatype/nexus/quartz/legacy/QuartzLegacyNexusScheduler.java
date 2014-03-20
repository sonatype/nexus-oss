/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.quartz.legacy;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.internal.LegacyNexusScheduler;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.HourlySchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.sisu.Priority;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobKey.jobKey;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerKey.triggerKey;

/**
 * The Quartz backed {@link LegacyNexusScheduler} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
@Priority(100)
public class QuartzLegacyNexusScheduler
    extends ComponentSupport
    implements LegacyNexusScheduler
{
  private final QuartzSupport quartzSupport;

  private final Map<String, Provider<NexusTask<?>>> legacyTasks;

  @Inject
  public QuartzLegacyNexusScheduler(final QuartzSupport quartzSupport,
                                    final Map<String, Provider<NexusTask<?>>> legacyTasks)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.legacyTasks = checkNotNull(legacyTasks);
  }

  @Override
  public void initializeTasks() {
    // nop
  }

  @Override
  public void shutdown() {
    // nop
  }

  @Override
  public <T> ScheduledTask<T> submit(final String name, final NexusTask<T> nexusTask)
      throws RejectedExecutionException, NullPointerException
  {
    if (nexusTask.allowConcurrentSubmission(getActiveTasks())) {
      final JobDataMap jobDataMap = new JobDataMap(nexusTask.getParameters());
      final JobDetail jobDetail = JobBuilder.newJob()
          .ofType(LegacyWrapperJob.class)
          .withDescription(name)
          .usingJobData(jobDataMap)
          .build();
      final Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKey(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()))
          .forJob(jobDetail.getKey())
          .startNow()
          .build();
      try {
        quartzSupport.getScheduler().scheduleJob(jobDetail, trigger);
        return new LegacyScheduledTask<T>(quartzSupport.getScheduler(), jobDetail, trigger);
      }
      catch (SchedulerException e) {
        throw new RejectedExecutionException("Could not submit job:", e);
      }
    }
    else {
      throw new RejectedExecutionException("Task of this type is already submitted!");
    }
  }

  @Override
  public <T> ScheduledTask<T> schedule(final String name, final NexusTask<T> nexusTask, final Schedule schedule)
      throws RejectedExecutionException, NullPointerException
  {
    if (nexusTask.allowConcurrentSubmission(getActiveTasks())) {
      final JobDataMap jobDataMap = new JobDataMap(nexusTask.getParameters());
      final JobDetail jobDetail = JobBuilder.newJob()
          .ofType(LegacyWrapperJob.class)
          .withDescription(name)
          .usingJobData(jobDataMap)
          .build();
      final Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKey(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()))
          .forJob(jobDetail.getKey())
          .withSchedule(toQuartzSchedule(schedule))
          .build();
      try {
        quartzSupport.getScheduler().scheduleJob(jobDetail, trigger);
        return new LegacyScheduledTask<T>(quartzSupport.getScheduler(), jobDetail, trigger);
      }
      catch (SchedulerException e) {
        throw new RejectedExecutionException("Could not submit job:", e);
      }
    }
    else {
      throw new RejectedExecutionException("Task of this type is already submitted!");
    }
  }

  @Override
  public <T> ScheduledTask<T> updateSchedule(final ScheduledTask<T> task)
      throws RejectedExecutionException, NullPointerException
  {
    if (task != null) {
      final JobDataMap jobDataMap = new JobDataMap(task.getTaskParams());
      final JobDetail jobDetail = JobBuilder.newJob()
          .ofType(LegacyWrapperJob.class)
          .withIdentity(jobKey(task.getId()))
          .withDescription(task.getName())
          .usingJobData(jobDataMap)
          .build();
      final Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKey(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()))
          .forJob(jobDetail.getKey())
          .withSchedule(toQuartzSchedule(task.getSchedule()))
          .build();
      try {
        quartzSupport.getScheduler().addJob(jobDetail, true);
        quartzSupport.getScheduler().rescheduleJob(trigger.getKey(), trigger);
        return new LegacyScheduledTask<T>(quartzSupport.getScheduler(), jobDetail, trigger);
      }
      catch (SchedulerException e) {
        throw new RejectedExecutionException("Could not submit job:", e);
      }
    }
    return task;
  }

  @Override
  public Map<String, List<ScheduledTask<?>>> getActiveTasks() {
    try {
      final List<JobExecutionContext> jobExecutionContexts = quartzSupport.getScheduler().getCurrentlyExecutingJobs();
      final Map<String, List<ScheduledTask<?>>> result = Maps.newHashMap();
      for (JobExecutionContext jobExecutionContext : jobExecutionContexts) {
        final JobDetail jobDetail = jobExecutionContext.getJobDetail();
        final Trigger trigger = jobExecutionContext.getTrigger();
        final LegacyScheduledTask st = new LegacyScheduledTask<>(quartzSupport.getScheduler(), jobDetail, trigger);
        if (!result.containsKey(st.getType())) {
          result.put(st.getType(), Lists.<ScheduledTask<?>>newArrayList());
        }
        result.get(st.getType()).add(st);
      }
      return result;
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Map<String, List<ScheduledTask<?>>> getAllTasks() {
    try {
      final Set<JobKey> jobKeys = quartzSupport.getScheduler().getJobKeys(GroupMatcher.<JobKey>anyGroup());
      final Map<String, List<ScheduledTask<?>>> result = Maps.newHashMap();
      for (JobKey jobKey : jobKeys) {
        final JobDetail jobDetail = quartzSupport.getScheduler().getJobDetail(jobKey);
        final Trigger trigger = quartzSupport.getScheduler().getTrigger(triggerKey(jobKey.getName()));
        final LegacyScheduledTask st = new LegacyScheduledTask<>(quartzSupport.getScheduler(), jobDetail, trigger);
        if (!result.containsKey(st.getType())) {
          result.put(st.getType(), Lists.<ScheduledTask<?>>newArrayList());
        }
        result.get(st.getType()).add(st);
      }
      return result;
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public ScheduledTask<?> getTaskById(final String id)
      throws NoSuchTaskException
  {
    try {
      final JobDetail jobDetail = quartzSupport.getScheduler().getJobDetail(jobKey(id));
      final Trigger trigger = quartzSupport.getScheduler().getTrigger(triggerKey(id));
      return new LegacyScheduledTask<>(quartzSupport.getScheduler(), jobDetail, trigger);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }

  }

  @Override
  public NexusTask<?> createTaskInstance(String taskType) throws IllegalArgumentException {
    return lookupTask(taskType);
  }

  @Override
  public <T> T createTaskInstance(final Class<T> taskType) throws IllegalArgumentException {
    log.debug("Creating task: {}", taskType);

    try {
      // first try a full class name lookup (modern sisu-style)
      return (T) lookupTask(taskType.getCanonicalName());
    }
    catch (IllegalArgumentException e) {
      // fallback to old plexus hint style
      return (T) lookupTask(taskType.getSimpleName());
    }
  }

  // ==

  private NexusTask<?> lookupTask(final String taskType) {
    log.debug("Looking up task for: " + taskType);
    final Provider<NexusTask<?>> taskProvider = legacyTasks.get(taskType);
    if (taskProvider == null) {
      throw new IllegalArgumentException("Could not find task of type: " + taskType);
    }
    final NexusTask<?> result = taskProvider.get();
    result.addParameter(LegacyWrapperJob.LEGACY_JOB_TYPE_KEY, taskType);
    return result;
  }

  private ScheduleBuilder<? extends Trigger> toQuartzSchedule(final Schedule schedule) {
    if (schedule instanceof OnceSchedule || schedule instanceof RunNowSchedule) {
      // TODO: replace with proper trigger
      return simpleSchedule();
    }
    else if (schedule instanceof HourlySchedule) {
      // TODO: replace with proper trigger
      return simpleSchedule()
          .withIntervalInHours(1)
          .repeatForever();
    }
    else if (schedule instanceof DailySchedule) {
      // TODO: replace with proper trigger
      return simpleSchedule()
          .withIntervalInHours(24)
          .repeatForever();
    }
    else if (schedule instanceof WeeklySchedule) {
      // TODO: replace with proper trigger
      return simpleSchedule()
          .withIntervalInHours(168)
          .repeatForever();
    }
    else if (schedule instanceof MonthlySchedule) {
      // TODO: replace with proper trigger
      return simpleSchedule()
          .withIntervalInHours(5040)
          .repeatForever();
    }
    else if (schedule instanceof CronSchedule) {
      return cronSchedule(((CronSchedule) schedule).getCronString());
    }
    else if (schedule instanceof ManualRunSchedule) {
      // TODO: ???
      return simpleSchedule();
    }
    throw new IllegalArgumentException("Unknown Nexus Schedule: " + schedule.getClass().getCanonicalName());
  }

}

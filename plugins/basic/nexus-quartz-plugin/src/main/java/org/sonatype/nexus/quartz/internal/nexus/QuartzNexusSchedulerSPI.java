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
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.nexus.scheduling.spi.NexusTaskExecutorSPI;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.sisu.Priority;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.KeyMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

/**
 * Quartz backed implementation of {@link NexusTaskExecutorSPI}. It uses distinct group for NX tasks and relies on
 * {@link QuartzSupport}.
 *
 * @since 3.0
 */
@Singleton
@Named
@Priority(2000)
public class QuartzNexusSchedulerSPI
    extends ComponentSupport
    implements NexusTaskExecutorSPI
{
  /**
   * Group for job and trigger keys.
   */
  static final String QZ_NEXUS_GROUP = "nexus";

  private final QuartzSupport quartzSupport;

  private final NexusScheduleConverter nexusScheduleConverter;

  @Inject
  public QuartzNexusSchedulerSPI(final QuartzSupport quartzSupport,
                                 final NexusScheduleConverter nexusScheduleConverter)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.nexusScheduleConverter = checkNotNull(nexusScheduleConverter);
  }

  @Override
  public <T> TaskInfo<T> getTaskById(final String id) {
    try {
      final JobKey jobKey = JobKey.jobKey(id, QZ_NEXUS_GROUP);
      return (TaskInfo<T>) allTasks().get(jobKey);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public List<TaskInfo<?>> listsTasks() {
    try {
      return Lists.<TaskInfo<?>>newArrayList(allTasks().values());
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Returns a jobKey to NexusTaskInfo map of all existing tasks.
   */
  private Map<JobKey, NexusTaskInfo<?>> allTasks() throws SchedulerException {
    final Set<JobKey> jobKeys = quartzSupport.getScheduler().getJobKeys(jobGroupEquals(QZ_NEXUS_GROUP));
    // put all task infos
    final Map<JobKey, NexusTaskInfo<?>> infos = Maps.newHashMap();
    for (JobKey jobKey : jobKeys) {
      infos.put(jobKey, new NexusTaskInfo<>(quartzSupport, jobKey, nexusScheduleConverter));
    }
    return infos;
  }

  @Override
  public <T> TaskInfo<T> scheduleTask(final TaskConfiguration taskConfiguration,
                                      final Schedule schedule)
  {
    try {
      final JobKey jobKey = JobKey.jobKey(taskConfiguration.getId(), QZ_NEXUS_GROUP);
      if (quartzSupport.getScheduler().checkExists(jobKey)) {
        // this is update
        checkState(!quartzSupport.isRunning(jobKey), "Task %s is currently running");
        quartzSupport.getScheduler().deleteJob(jobKey);
      }
      final JobDataMap jobDataMap = new JobDataMap(taskConfiguration.getMap());
      final JobDetail jobDetail = JobBuilder.newJob(NexusTaskJobSupport.class).withIdentity(jobKey)
          .withDescription(taskConfiguration.getName()).usingJobData(jobDataMap).build();

      // get trigger, but use identity of jobKey
      // This is only for simplicity, as is not a requirement: NX job:triggers are 1:1 so tying them as this is ok
      final Trigger trigger = nexusScheduleConverter.toTrigger(schedule).getTriggerBuilder()
          .withIdentity(jobKey.getName(), jobKey.getGroup()).build();

      // register job specific listener
      quartzSupport.getScheduler().getListenerManager()
          .addJobListener(new NexusTaskJobListener<>(quartzSupport, jobKey),
              KeyMatcher.keyEquals(jobKey));
      // schedule the job
      quartzSupport.getScheduler().scheduleJob(jobDetail, trigger);
      // enabled
      if (!taskConfiguration.isEnabled()) {
        quartzSupport.getScheduler().pauseJob(jobKey);
      }
      return new NexusTaskInfo<>(quartzSupport, jobKey, nexusScheduleConverter);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public boolean removeTask(final String id) {
    try {
      final JobKey jobKey = JobKey.jobKey(id, QZ_NEXUS_GROUP);
      return quartzSupport.getScheduler().deleteJob(jobKey);
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public int getRunnintTaskCount() {
    try {
      return quartzSupport.getScheduler().getCurrentlyExecutingJobs().size();
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }
}

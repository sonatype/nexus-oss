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

package org.sonatype.nexus.quartz.internal;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.internal.NexusSchedulerSPI;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.matchers.KeyMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Quartz backed implementation of {@link DefaultNexusSchedulerSPI}.
 */
@Singleton
@Named
public class DefaultNexusSchedulerSPI
    extends ComponentSupport
    implements NexusSchedulerSPI
{
  private static final String QZ_NEXUS_GROUP = "nexus";

  private final QuartzSupport quartzSupport;

  @Inject
  public DefaultNexusSchedulerSPI(final QuartzSupport quartzSupport) {
    this.quartzSupport = checkNotNull(quartzSupport);
  }

  @Override
  public <T> Future<T> submit(final NexusTask<T> nexusTask) throws RejectedExecutionException, NullPointerException {
    try {
      final JobKey jobKey = JobKey.jobKey(nexusTask.getId(), QZ_NEXUS_GROUP);
      final JobDataMap jobDataMap = new JobDataMap(nexusTask.getParameters());
      jobDataMap.put(NexusTaskJobSupport.NX_TASK_FQCN, nexusTask.getClass().getName());
      final JobDetail jobDetail = JobBuilder.newJob(NexusTaskJobSupport.class).withIdentity(jobKey)
          .withDescription(nexusTask.getName()).usingJobData(jobDataMap).build();

      final Trigger trigger = null;
      final NexusTaskJobListener<T> nexusTaskJobListener = new NexusTaskJobListener<>(quartzSupport, nexusTask, jobKey);
      quartzSupport.getScheduler().getListenerManager()
          .addJobListener(nexusTaskJobListener, KeyMatcher.keyEquals(jobKey));
      quartzSupport.getScheduler().scheduleJob(jobDetail, trigger);
      return nexusTaskJobListener;
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public int getRunningTaskCount() {
    try {
      return quartzSupport.getScheduler().getCurrentlyExecutingJobs().size();
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void killAll() {
    try {
      final List<JobExecutionContext> currentlyRunning = quartzSupport.getScheduler().getCurrentlyExecutingJobs();
      for (JobExecutionContext context : currentlyRunning) {
        try {
          quartzSupport.getScheduler().interrupt(context.getJobDetail().getKey());
        }
        catch (UnableToInterruptJobException e) {
          log.info("Cannot interrupt job {} : {}", context.getJobDetail().getKey(),
              context.getJobDetail().getJobClass(), e);
        }
      }
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }
}

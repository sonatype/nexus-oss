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

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.TaskInfo.EndState;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {#link JobListenerSupport} that provides NX Task integration by creating future when task starts, recording
 * execution results.
 *
 * @since 3.0
 */
public class NexusTaskJobListener<T>
    extends JobListenerSupport
{
  private final QuartzSupport quartzSupport;

  private final JobKey jobKey;

  private volatile long startedAt;

  private volatile NexusTaskFuture<T> future;

  public NexusTaskJobListener(final QuartzSupport quartzSupport,
                              final JobKey jobKey)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
  }

  // == JobListener

  @Override
  public void jobToBeExecuted(final JobExecutionContext context) {
    future = new NexusTaskFuture<>(quartzSupport, jobKey);
    context.put(NexusTaskFuture.FUTURE_KEY, future);
    startedAt = context.getFireTime().getTime();
  }

  @Override
  public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
    try {
      quartzSupport.getScheduler().getListenerManager().removeJobListener(getName());
      if (context.getTrigger().getNextFireTime() == null) {
        context.getScheduler().deleteJob(jobKey);
      }
    }
    catch (SchedulerException e) {
      // mute
    }
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
    jobDataMap.putAsString("lastRunState.runStarted", startedAt);
    jobDataMap.putAsString("lastRunState.runDuration", System.currentTimeMillis() - startedAt);
    future.setResult((T) context.getResult(), jobException);
  }

  @Override
  public String getName() {
    return jobKey.getName();
  }
}
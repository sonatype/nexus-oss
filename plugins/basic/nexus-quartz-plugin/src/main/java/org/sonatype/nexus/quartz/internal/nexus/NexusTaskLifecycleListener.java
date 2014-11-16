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

package org.sonatype.nexus.quartz.internal.nexus;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskLifecycleListener;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adapter for NX {@link TaskLifecycleListener} into quartz {@link JobListenerSupport}.
 *
 * @since 3.0
 */
public class NexusTaskLifecycleListener<T>
    extends JobListenerSupport
{
  private final TaskLifecycleListener taskLifecycleListener;

  private final QuartzSupport quartzSupport;

  private final NexusScheduleConverter nexusScheduleConverter;

  public NexusTaskLifecycleListener(final TaskLifecycleListener taskLifecycleListener,
                                    final QuartzSupport quartzSupport,
                                    final NexusScheduleConverter nexusScheduleConverter)
  {
    this.taskLifecycleListener = checkNotNull(taskLifecycleListener);
    this.quartzSupport = checkNotNull(quartzSupport);
    this.nexusScheduleConverter = checkNotNull(nexusScheduleConverter);
  }

  // == JobListener

  @Override
  public void jobToBeExecuted(final JobExecutionContext context) {
    taskLifecycleListener
        .onTaskStarted(new NexusTaskInfo<>(quartzSupport, context.getJobDetail().getKey(), nexusScheduleConverter));
  }

  @Override
  public void jobWasExecuted(final JobExecutionContext context, final JobExecutionException jobException) {
    final TaskInfo<?> taskInfo = new NexusTaskInfo<>(quartzSupport, context.getJobDetail().getKey(),
        nexusScheduleConverter);
    final NexusTaskFuture<?> future = (NexusTaskFuture) context.get(NexusTaskFuture.FUTURE_KEY);
    if (jobException != null) {
      taskLifecycleListener.onTaskStoppedStoppedFailed(taskInfo, jobException);
    }
    else if (future != null && future.isCancelled()) {
      taskLifecycleListener.onTaskStoppedStoppedCanceled(taskInfo);
    }
    else {
      taskLifecycleListener.onTaskStoppedStopped(taskInfo);
    }
  }

  @Override
  public String getName() {
    return taskLifecycleListener.getClass().getName();
  }
}
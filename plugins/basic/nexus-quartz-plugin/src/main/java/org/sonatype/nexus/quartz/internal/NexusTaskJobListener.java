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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.scheduling.NexusTask;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link NexusTask} support class for Quartz.
 *
 * @since 3.0
 */
public class NexusTaskJobListener<T>
    extends JobListenerSupport
    implements Future<T>
{
  private final QuartzSupport quartzSupport;

  private final NexusTask<T> nexusTask;

  private final JobKey jobKey;

  private final CountDownLatch countDownLatch;

  private volatile boolean canceled;

  private JobExecutionException jobExecutionException;

  private T result;

  public NexusTaskJobListener(final QuartzSupport quartzSupport,
                              final NexusTask<T> nexusTask,
                              final JobKey jobKey)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.nexusTask = checkNotNull(nexusTask);
    this.jobKey = checkNotNull(jobKey);
    this.countDownLatch = new CountDownLatch(1);
  }

  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    result = (T) context.get(NexusTaskJobSupport.NX_TASK_RESULT);
    this.jobExecutionException = jobException;
    try {
      quartzSupport.getScheduler().getListenerManager().removeJobListener(getName());
    }
    catch (SchedulerException e) {
      // mute
    }
    countDownLatch.countDown();
  }

  @Override
  public String getName() {
    return nexusTask.getId();
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return canceled = quartzSupport.removeJob(jobKey);
  }

  @Override
  public boolean isCancelled() {
    return canceled;
  }

  @Override
  public boolean isDone() {
    return countDownLatch.getCount() == 0;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    countDownLatch.await();
    if (jobExecutionException != null) {
      throw new ExecutionException("Job failure: ", jobExecutionException);
    }
    return result;
  }

  @Override
  public T get(final long timeout, final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException
  {
    countDownLatch.await(timeout, unit);
    if (jobExecutionException != null) {
      throw new ExecutionException("Job failure: ", jobExecutionException);
    }
    return result;
  }
}
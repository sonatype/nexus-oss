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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.sonatype.nexus.quartz.QuartzSupport;

import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nexus task {@link Future}. Is created by {@link NexusTaskJobListener} and is stick into job context.
 *
 * @since 3.0
 */
public class NexusTaskFuture<T>
    implements Future<T>
{
  /**
   * Key used in job execution context to stick future in.
   */
  static final String FUTURE_KEY = NexusTaskFuture.class.getName();

  private final QuartzSupport quartzSupport;

  private final JobKey jobKey;

  private final CountDownLatch countDownLatch;

  private volatile boolean canceled;

  private JobExecutionException jobExecutionException;

  private T result;

  public NexusTaskFuture(final QuartzSupport quartzSupport,
                         final JobKey jobKey)
  {
    this.quartzSupport = checkNotNull(quartzSupport);
    this.jobKey = checkNotNull(jobKey);
    this.countDownLatch = new CountDownLatch(1);
  }

  public void setResult(final T result, final JobExecutionException jobExecutionException) {
    this.result = result;
    this.jobExecutionException = jobExecutionException;
    countDownLatch.countDown();
  }

  // == TaskFuture

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    canceled = true;
    return quartzSupport.cancelJob(jobKey);
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
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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.threads.FakeAlmightySubject;
import org.sonatype.nexus.threads.NexusExecutorService;
import org.sonatype.nexus.threads.NexusThreadFactory;

import com.google.common.base.Throwables;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

/**
 * Nexus specific implementation of {@link ThreadPool} that is integrated with Shiro.
 *
 * @since 3.0
 */
public class NexusThreadPool
    implements ThreadPool
{
  private final ThreadPoolExecutor threadPoolExecutor;

  private final NexusExecutorService nexusExecutorService;

  public NexusThreadPool(final int poolSize) {
    this.threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(poolSize), // bounded queue
        new NexusThreadFactory("qz", "nx-tasks"),
        new AbortPolicy());
    // wrapper for Shiro integration
    this.nexusExecutorService = NexusExecutorService
        .forFixedSubject(threadPoolExecutor, FakeAlmightySubject.TASK_SUBJECT);
  }

  @Override
  public boolean runInThread(final Runnable runnable) {
    try {
      nexusExecutorService.submit(runnable);
      return true;
    }
    catch (RejectedExecutionException e) {
      return false;
    }
  }

  @Override
  public int blockForAvailableThreads() {
    threadPoolExecutor.purge();
    return Math.max(0, threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount());
  }

  @Override
  public void initialize() throws SchedulerConfigException {
    // nop
  }

  @Override
  public void shutdown(final boolean waitForJobsToComplete) {
    nexusExecutorService.shutdown();
    if (waitForJobsToComplete) {
      try {
        nexusExecutorService.awaitTermination(5L, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  @Override
  public int getPoolSize() {
    return threadPoolExecutor.getPoolSize();
  }

  @Override
  public void setInstanceId(final String schedInstId) {
    // ?
  }

  @Override
  public void setInstanceName(final String schedName) {
    // ?
  }
}

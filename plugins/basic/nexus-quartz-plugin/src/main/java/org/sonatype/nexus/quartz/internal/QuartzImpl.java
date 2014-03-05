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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.Quartz;
import org.sonatype.nexus.quartz.internal.store.KVJobStore;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Throwables;
import org.eclipse.sisu.BeanEntry;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Quartz}.
 *
 * @since 2.8
 */
@Singleton
@Named
public class QuartzImpl
    extends LifecycleSupport
    implements Quartz, JobFactory
{
  private final Iterable<BeanEntry<Named, Job>> jobEntries;

  private final KVJobStore kazukiJobStore;

  private Scheduler scheduler;

  private boolean active;

  private int threadPoolSize;

  @Inject
  public QuartzImpl(final Iterable<BeanEntry<Named, Job>> jobEntries,
                    final KVJobStore kazukiJobStore)
      throws Exception
  {
    this.jobEntries = checkNotNull(jobEntries);
    this.kazukiJobStore = checkNotNull(kazukiJobStore);
    this.active = true;
    this.threadPoolSize = 20;
  }

  @Override
  protected void doStart() throws Exception {
    // create Scheduler
    DirectSchedulerFactory.getInstance()
        .createScheduler(new SimpleThreadPool(threadPoolSize, Thread.NORM_PRIORITY), kazukiJobStore);
    scheduler = DirectSchedulerFactory.getInstance().getScheduler();
    scheduler.setJobFactory(this);
    if (active) {
      scheduler.start();
      log.info("Quartz Scheduler created and started.");
    }
    else {
      log.info("Quartz Scheduler created.");
    }
  }

  @Override
  protected void doStop() throws Exception {
    scheduler.shutdown();
    scheduler = null;
    log.info("Quartz Scheduler stopped.");
  }

  /**
   * Returns {@code true} if scheduler is started and is ready (Quartz is started, is not in "stand-by" mode not shut
   * down)..
   */
  public boolean isActive() {
    try {
      // Quartz peculiarity: isStarted is TRUE if method was invoked at all (even if it's followed by stand-by or shut down)
      return scheduler != null && scheduler.isStarted() && !scheduler.isInStandbyMode() && !scheduler.isShutdown();
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Sets the {@code active} flag. Quartz, if created, will be managed also (put in stand-by or started), depending on
   * the value of the {@code active} flag.
   */
  public void setActive(final boolean active) throws Exception {
    this.active = active;
    if (scheduler != null) {
      if (!active && scheduler.isStarted()) {
        scheduler.standby();
        log.info("Scheduler put into stand-by mode");
      }
      else if (active && scheduler.isInStandbyMode()) {
        scheduler.start();
        log.info("Scheduler put into ready mode");
      }
    }
  }

  /**
   * Returns the actual size of the pool being used by Quartz scheduler.
   */
  public int getThreadPoolSize() {
    return threadPoolSize;
  }

  /**
   * Sets the thread pool size to be used with Quartz scheduler. <em>This method has effect only if is called
   * before {@link #start()}!</em>, otherwise the call is completely neglected.
   */
  public void setThreadPoolSize(final int threadPoolSize) {
    if (scheduler == null) {
      this.threadPoolSize = threadPoolSize;
    }
  }

  // JobFactory

  @Override
  public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
    final BeanEntry<Named, Job> jobEntry = locate(bundle.getJobDetail().getJobClass());
    if (jobEntry != null) {
      return jobEntry.getProvider().get(); // to support not-singletons
    }
    throw new SchedulerException("Cannot create new instance of Job: " + bundle.getJobDetail().getJobClass().getName());
  }

  private BeanEntry<Named, Job> locate(final Class<? extends Job> jobClass) {
    for (BeanEntry<Named, Job> jobEntry : jobEntries) {
      if (jobEntry.getImplementationClass().equals(jobClass)) {
        return jobEntry;
      }
    }
    return null;
  }


  // Public API

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override

  public <T extends Job> JobKey execute(final Class<T> clazz) {
    return scheduleJob(clazz, TriggerBuilder.newTrigger().startNow().build());
  }

  @Override
  public <T extends Job> JobKey scheduleJob(final Class<T> clazz, final Trigger trigger) {
    final BeanEntry<Named, Job> jobEntry = locate(clazz);
    final JobDetail jobDetail = JobBuilder.newJob().ofType(clazz).withDescription(
        jobEntry.getDescription()).build();
    try {
      scheduler.scheduleJob(jobDetail, trigger);
    }
    catch (SchedulerException e) {
      Throwables.propagate(e);
    }
    return jobDetail.getKey();
  }
}

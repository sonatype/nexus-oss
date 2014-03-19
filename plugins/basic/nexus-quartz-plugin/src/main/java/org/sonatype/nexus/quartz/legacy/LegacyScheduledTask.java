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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.sonatype.scheduling.ProgressListener;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;

import org.quartz.JobDetail;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Legacy {@link ScheduledTask}.
 *
 * @since 3.0
 */
public class LegacyScheduledTask<T>
    implements ScheduledTask<T>
{
  private final JobDetail jobDetail;

  public LegacyScheduledTask(final JobDetail jobDetail) {
    this.jobDetail = checkNotNull(jobDetail);
  }

  @Override
  public SchedulerTask<T> getSchedulerTask() {
    return null;
  }

  @Override
  public ProgressListener getProgressListener() {
    return null;
  }

  @Override
  public Callable<T> getTask() {
    return null;
  }

  @Override
  public String getId() {
    return jobDetail.getKey().getName();
  }

  @Override
  public String getName() {
    return jobDetail.getDescription();
  }

  @Override
  public void setName(final String name) {
  }

  @Override
  public String getType() {
    return String.valueOf(jobDetail.getJobDataMap().get(LegacyWrapperJob.LEGACY_JOB_TYPE_KEY));
  }

  @Override
  public TaskState getTaskState() {
    return null;
  }

  @Override
  public Date getScheduledAt() {
    return null;
  }

  @Override
  public void runNow() {

  }

  @Override
  public void cancelOnly() {

  }

  @Override
  public void cancel() {

  }

  @Override
  public void cancel(final boolean interrupt) {

  }

  @Override
  public void reset() {

  }

  @Override
  public Throwable getBrokenCause() {
    return null;
  }

  @Override
  public T get() throws ExecutionException, InterruptedException {
    return null;
  }

  @Override
  public T getIfDone() {
    return null;
  }

  @Override
  public Date getLastRun() {
    return null;
  }

  @Override
  public TaskState getLastStatus() {
    return null;
  }

  @Override
  public Long getDuration() {
    return null;
  }

  @Override
  public Date getNextRun() {
    return null;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public void setEnabled(final boolean enabled) {

  }

  @Override
  public List<T> getResults() {
    return null;
  }

  @Override
  public SchedulerIterator getScheduleIterator() {
    return null;
  }

  @Override
  public Schedule getSchedule() {
    return null;
  }

  @Override
  public void setSchedule(final Schedule schedule) {

  }

  @Override
  public Map<String, String> getTaskParams() {
    return null;
  }
}

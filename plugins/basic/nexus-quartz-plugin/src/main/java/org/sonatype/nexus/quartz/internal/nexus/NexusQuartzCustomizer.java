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
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.QuartzSupport;
import org.sonatype.nexus.quartz.internal.QuartzCustomizer;
import org.sonatype.nexus.scheduling.TaskLifecycleListener;

import com.google.common.base.Throwables;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.matchers.KeyMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

/**
 * Customizer installing NX specific JobListeners on Quartz being created. This is required as while Jobs are persisted
 * in JobStore, JobListeners that provide NX integrations are not, hence, on boot, these should be hooked in back.
 *
 * @since 3.0
 */
@Singleton
@Named
public class NexusQuartzCustomizer
    extends QuartzCustomizer
{
  private final NexusScheduleConverter nexusScheduleConverter;

  private final List<TaskLifecycleListener> taskLifecycleListeners;

  @Inject
  public NexusQuartzCustomizer(final NexusScheduleConverter nexusScheduleConverter,
                               final List<TaskLifecycleListener> taskLifecycleListeners)
  {
    this.nexusScheduleConverter = checkNotNull(nexusScheduleConverter);
    this.taskLifecycleListeners = checkNotNull(taskLifecycleListeners);
  }

  @Override
  public void onCreated(final QuartzSupport quartzSupport, final Scheduler scheduler) {
    try {
      // Install job supporting listeners for each NX task being scheduled
      final Set<JobKey> jobKeys = scheduler.getJobKeys(jobGroupEquals(DefaultNexusSchedulerSPI.QZ_NEXUS_GROUP));
      for (JobKey jobKey : jobKeys) {
        final NexusTaskJobListener<?> listener = new NexusTaskJobListener<>(quartzSupport, nexusScheduleConverter,
            jobKey);
        scheduler.getListenerManager().addJobListener(listener, KeyMatcher.keyEquals(jobKey));
      }

      // Install global listeners
      for (TaskLifecycleListener taskLifecycleListener : taskLifecycleListeners) {
        final NexusTaskLifecycleListener listener = new NexusTaskLifecycleListener(taskLifecycleListener, quartzSupport,
            nexusScheduleConverter);
        scheduler.getListenerManager().addJobListener(listener);
      }
    }
    catch (SchedulerException e) {
      throw Throwables.propagate(e);
    }
  }
}
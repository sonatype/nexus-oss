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

package org.sonatype.nexus.analytics.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.capability.support.CapabilitySupport;
import org.sonatype.nexus.plugins.capabilities.Condition;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Analytics automatic submission capability.
 *
 * @since 2.8
 */
@Named(AutoSubmitCapabilityDescriptor.TYPE_ID)
public class AutoSubmitCapability
    extends CapabilitySupport<AutoSubmitCapabilityConfiguration>
{
  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Submission enabled")
    String description();

    @DefaultMessage("Submission disabled")
    String disabledDescription();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final NexusScheduler scheduler;

  private final Provider<SubmitTask> taskFactory;

  @Inject
  public AutoSubmitCapability(final NexusScheduler scheduler,
                              final Provider<SubmitTask> taskFactory)
  {
    this.scheduler = checkNotNull(scheduler);
    this.taskFactory = checkNotNull(taskFactory);
  }

  @Override
  protected AutoSubmitCapabilityConfiguration createConfig(final Map<String, String> properties) throws Exception {
    return new AutoSubmitCapabilityConfiguration(properties);
  }

  @Override
  public Condition activationCondition() {
    return conditions().logical().and(
        // collection capability must be active
        conditions().capabilities().capabilityOfTypeActive(CollectionCapabilityDescriptor.TYPE),
        conditions().capabilities().passivateCapabilityDuringUpdate()
    );
  }

  /**
   * Get the {@link SubmitTask} scheduled task;
   */
  private ScheduledTask<?> getTask() throws Exception {
    ScheduledTask<?> scheduled;

    List<ScheduledTask<?>> tasks = tasksForTypeId(SubmitTask.ID);
    if (tasks.isEmpty()) {
      // create new task
      SubmitTask task = taskFactory.get();
      // default run at 1AM daily
      Schedule schedule = new CronSchedule("0 0 1 * * ?");
      scheduled = scheduler.schedule("Automatically submit analytics events", task, schedule);
      log.debug("Created task: {}", scheduled);
    }
    else {
      // complain if there is more than one
      if (tasks.size() != 1) {
        log.warn("More than 1 task found, additional tasks should be removed");
      }
      scheduled = tasks.get(0);

      // complain is task is set to manual, as this won't actually automatically do anything
      Schedule schedule = scheduled.getSchedule();
      if (schedule instanceof ManualRunSchedule) {
        log.warn("Task schedule is set to manual");
      }
    }

    return scheduled;
  }

  /**
   * Helper to get all tasks for a given scheduled task type-id.
   */
  private List<ScheduledTask<?>> tasksForTypeId(final String typeId) {
    for (Entry<String, List<ScheduledTask<?>>> entry : scheduler.getActiveTasks().entrySet()) {
      if (typeId.equals(entry.getKey())) {
        return entry.getValue();
      }
    }
    return Collections.emptyList();
  }

  // FIXME: Can not reference scheduled tasks onLoad, this happens too early
  // FIXME: Replace with NexusStarted event handler to keep state in sync?
  // FIXME: Additionally, perhaps the SubmitTask should verify that the capability is enabled before running?

  //@Override
  //protected void onLoad(final AutoSubmitCapabilityConfiguration config) throws Exception {
  //  // disable the task, to keep state in sync in case of user manually toggling state
  //  ScheduledTask task = getTask();
  //  if (task.isEnabled()) {
  //    task.setEnabled(false);
  //    scheduler.updateSchedule(task);
  //  }
  //}

  @Override
  protected void onActivate(final AutoSubmitCapabilityConfiguration config) throws Exception {
    // enable the task
    ScheduledTask task = getTask();
    task.setEnabled(true);
    scheduler.updateSchedule(task);
    log.info("Automatic submission enabled");
  }

  @Override
  protected void onPassivate(final AutoSubmitCapabilityConfiguration config) throws Exception {
    // disable the task
    ScheduledTask task = getTask();
    task.setEnabled(false);
    scheduler.updateSchedule(task);
    log.info("Automatic submission disabled");
  }

  @Override
  protected void onRemove(final AutoSubmitCapabilityConfiguration config) throws Exception {
    // should only be 1 task, but for sanity cancel any of this type
    for (ScheduledTask<?> task : tasksForTypeId(SubmitTask.ID)) {
      task.cancel();
    }
  }

  @Override
  protected String renderDescription() throws Exception {
    if (!context().isActive()) {
      return messages.disabledDescription();
    }

    return messages.description();
  }
}

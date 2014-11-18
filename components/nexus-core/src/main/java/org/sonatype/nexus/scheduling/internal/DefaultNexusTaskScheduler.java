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

package org.sonatype.nexus.scheduling.internal;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.NexusTaskScheduler;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptor;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.nexus.scheduling.spi.NexusTaskExecutorSPI;
import org.sonatype.nexus.util.DigesterUtils;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Nexus task executor, that relies on SPI provider to execute tasks.
 *
 * @since 3.0
 */
@Singleton
@Named
public class DefaultNexusTaskScheduler
    extends ComponentSupport
    implements NexusTaskScheduler
{
  private final NexusTaskFactory nexusTaskFactory;

  private final List<TaskDescriptor> taskDescriptors;

  private final List<NexusTaskExecutorSPI> schedulers;

  @Inject
  public DefaultNexusTaskScheduler(final NexusTaskFactory nexusTaskFactory,
                                   final List<TaskDescriptor> taskDescriptors,
                                   final List<NexusTaskExecutorSPI> schedulers)
  {
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
    this.taskDescriptors = checkNotNull(taskDescriptors);
    this.schedulers = checkNotNull(schedulers);
  }

  protected NexusTaskExecutorSPI getScheduler() {
    if (schedulers.isEmpty()) {
      throw new IllegalStateException("No scheduler present in system!");
    }
    else {
      return schedulers.get(0);
    }
  }

  @Override
  public <T> TaskInfo<T> submit(final TaskConfiguration taskConfiguration) {
    return scheduleTask(taskConfiguration, new Now());
  }

  @Override
  public <T> TaskInfo<T> getTaskById(final String id) {
    checkNotNull(id);
    return getScheduler().getTaskById(id);
  }

  @Override
  public List<TaskInfo<?>> listsTasks() {
    return getScheduler().listsTasks();
  }

  @Override
  public <T> TaskInfo<T> scheduleTask(final TaskConfiguration taskConfiguration, final Schedule schedule) {
    checkNotNull(taskConfiguration);
    taskConfiguration.validate();
    checkNotNull(schedule);
    final Date now = new Date();
    if (taskConfiguration.getCreated() != null) {
      taskConfiguration.setCreated(now);
    }
    taskConfiguration.setUpdated(now);
    return getScheduler().scheduleTask(taskConfiguration, schedule);
  }

  @Override
  public boolean removeTask(final String id) {
    checkNotNull(id);
    return getScheduler().removeTask(id);
  }

  @Override
  public <T extends Task> T createTaskInstance(final TaskConfiguration taskConfiguration)
      throws IllegalArgumentException
  {
    checkNotNull(taskConfiguration);
    return nexusTaskFactory.createTaskInstance(taskConfiguration);
  }

  @Override
  public TaskConfiguration createTaskConfigurationInstance(final Class<? extends Task> taskType)
      throws IllegalArgumentException
  {
    checkNotNull(taskType);
    return createTaskConfigurationInstance(taskType.getName());
  }

  @Override
  public TaskConfiguration createTaskConfigurationInstance(final String taskType) throws IllegalArgumentException {
    checkNotNull(taskType);
    checkArgument(nexusTaskFactory.isTask(taskType), "Type '%s' is not a task", taskType);
    // try to match a descriptor for class, and use that
    for (TaskDescriptor taskDescriptor : taskDescriptors) {
      if (taskDescriptor.getType().getName().equals(taskType)) {
        return createTaskConfigurationInstanceFromDescriptor(taskDescriptor);
      }
    }
    // sane fallback for internal tasks not having descriptor
    log.debug("Creating task configuration for task class: {}", taskType);
    final TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId(generateId(taskType, taskConfiguration));
    taskConfiguration.setName(taskType);
    taskConfiguration.setType(taskType);
    taskConfiguration.setVisible(false); // tasks w/o descriptors are invisible in UI by default
    return taskConfiguration;
  }

  /**
   * Creates configuration from descriptor.
   */
  private TaskConfiguration createTaskConfigurationInstanceFromDescriptor(final TaskDescriptor taskDescriptor)
      throws IllegalArgumentException
  {
    log.debug("Creating task configuration for task descriptor: {}", taskDescriptor.getId());
    final TaskConfiguration taskConfiguration = new TaskConfiguration();
    taskConfiguration.setId(generateId(taskDescriptor.getType().getName(), taskConfiguration));
    taskConfiguration.setName(taskDescriptor.getName());
    taskConfiguration.setType(taskDescriptor.getType().getName());
    taskConfiguration.setVisible(taskDescriptor.isVisible());
    return taskConfiguration;
  }

  /**
   * Creates a unique ID for the task.
   */
  private String generateId(final String taskFQCName,
                            final TaskConfiguration taskConfiguration)
  {
    // TODO: call into quartz for this? Must not clash with existing persisted job IDs!
    return DigesterUtils.getSha1Digest(
        taskFQCName
            + System.identityHashCode(taskConfiguration)
            + System.nanoTime()
    );
  }

  // ==

  @Override
  @Deprecated
  public void killAll() {
    // TODO: nop, used in UTs only
  }

  @Override
  @Deprecated
  public int getRunningTaskCount() {
    int running = 0;
    final List<TaskInfo<?>> tasks = getScheduler().listsTasks();
    for (TaskInfo<?> task : tasks) {
      if (State.RUNNING == task.getCurrentState().getState()) {
        running++;
      }
    }
    return running;
  }
}

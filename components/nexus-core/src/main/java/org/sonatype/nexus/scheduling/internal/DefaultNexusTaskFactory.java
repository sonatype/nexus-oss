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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskDescriptor;
import org.sonatype.nexus.scheduling.TaskDescriptorSupport;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.sisu.BeanEntry;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link NexusTaskFactory} that hooks into SISU.
 *
 * @since 3.0
 */
@Singleton
@Named
public class DefaultNexusTaskFactory
    extends ComponentSupport
    implements NexusTaskFactory
{
  private final Iterable<BeanEntry<Named, Task>> tasks;

  private final List<TaskDescriptor<?>> taskDescriptors;

  @Inject
  public DefaultNexusTaskFactory(final Iterable<BeanEntry<Named, Task>> tasks,
                                 final List<TaskDescriptor<?>> taskDescriptors)
  {
    this.tasks = checkNotNull(tasks);
    this.taskDescriptors = checkNotNull(taskDescriptors);
  }

  @Override
  public List<TaskDescriptor<?>> listTaskDescriptors() {
    final Map<Class<? extends Task>, TaskDescriptor<?>> descriptorMap = Maps.newHashMap();
    for (TaskDescriptor<?> taskDescriptor : taskDescriptors) {
      descriptorMap.put(taskDescriptor.getType(), taskDescriptor);
    }
    for (BeanEntry<Named, Task> entry : tasks) {
      if (!descriptorMap.containsKey(entry.getImplementationClass())) {
        final TaskDescriptor<?> taskDescriptor = createTaskDescriptor(entry);
        descriptorMap.put(taskDescriptor.getType(), taskDescriptor);
      }
    }
    return Lists.newArrayList(descriptorMap.values());
  }

  @Override
  public <T extends Task> TaskDescriptor<T> resolveTaskDescriptorByTypeId(final String taskTypeId) {
    // look for descriptors first
    for (TaskDescriptor<?> taskDescriptor : taskDescriptors) {
      if (taskDescriptor.getId().equals(taskTypeId)) {
        return (TaskDescriptor<T>) taskDescriptor;
      }
    }
    // not found by descriptor, try tasks directly
    for (BeanEntry<Named, Task> entry : tasks) {
      // checks: task FQCN, task SimpleName or @Named
      if (entry.getImplementationClass().getName().equals(taskTypeId)
          || entry.getImplementationClass().getSimpleName().equals(taskTypeId)
          || entry.getKey().value().equals(taskTypeId)) {
        return (TaskDescriptor<T>) findTaskDescriptor(entry);
      }
    }
    return null;
  }

  @Override
  public Task<?> createTaskInstance(final TaskConfiguration taskConfiguration)
      throws IllegalArgumentException
  {
    checkNotNull(taskConfiguration);
    taskConfiguration.validate();
    log.debug("Creating task by hint: {}", taskConfiguration);
    final TaskDescriptor<?> taskDescriptor = resolveTaskDescriptorByTypeId(taskConfiguration.getTypeId());
    checkArgument(taskDescriptor != null, "Unknown taskType: '%s'", taskConfiguration.getTypeId());
    taskConfiguration.setTypeId(taskDescriptor.getId());
    for (BeanEntry<Named, Task> entry : tasks) {
      if (entry.getImplementationClass().equals(taskDescriptor.getType())) {
        final Task task = entry.getProvider().get();
        task.getConfiguration().getMap().putAll(taskConfiguration.getMap());
        return task;
      }
    }
    throw new IllegalArgumentException("No Task of type \'" + taskConfiguration.getTypeId() + "\' found");
  }

  // ==

  /**
   * Returns {@link TaskDescriptor} by given Task's bean entry. Will perform a search for provided task descriptors,
   * and if not found, will create one using {@link #createTaskDescriptor(BeanEntry)}.
   */
  private <T extends Task> TaskDescriptor<T> findTaskDescriptor(final BeanEntry<Named, Task> taskBeanEntry) {
    // look for descriptors first
    for (TaskDescriptor<?> taskDescriptor : taskDescriptors) {
      if (taskDescriptor.getType().equals(taskBeanEntry.getImplementationClass())) {
        return (TaskDescriptor<T>) taskDescriptor;
      }
    }
    // not found by descriptor, try tasks directly
    return (TaskDescriptor<T>) createTaskDescriptor(taskBeanEntry);
  }

  /**
   * Creates {@link TaskDescriptor} for given Task's bean entry class. To be used For tasks without descriptors, it
   * will create one on the fly with defaults.
   */
  private <T extends Task> TaskDescriptor<T> createTaskDescriptor(final BeanEntry<Named, Task> taskBeanEntry) {
    final String taskName =
        taskBeanEntry.getDescription() != null
            ? taskBeanEntry.getDescription()
            : taskBeanEntry.getImplementationClass().getSimpleName();
    // by default, tasks w/o descriptors are not exposed, and not visible while run/scheduled
    return new TaskDescriptorSupport<T>(
        (Class<T>) taskBeanEntry.getImplementationClass(),
        taskName,
        false,
        false
    ) { };
  }
}

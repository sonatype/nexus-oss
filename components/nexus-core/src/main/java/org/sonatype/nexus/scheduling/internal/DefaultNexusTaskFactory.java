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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
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

  @Inject
  public DefaultNexusTaskFactory(final Iterable<BeanEntry<Named, Task>> tasks)
  {
    this.tasks = checkNotNull(tasks);
  }

  @Override
  public Task<?> createTaskInstance(final TaskConfiguration taskConfiguration)
      throws IllegalArgumentException
  {
    checkNotNull(taskConfiguration);
    checkArgument(!Strings.isNullOrEmpty(taskConfiguration.getId()), "Invalid task configuration: id");
    checkArgument(!Strings.isNullOrEmpty(taskConfiguration.getType()), "Invalid task configuration: type");
    log.debug("Creating task by hint: {}", taskConfiguration);
    for (BeanEntry<Named, Task> entry : tasks) {
      if (entry.getImplementationClass().getName().equals(taskConfiguration.getType())) {
        final Task task = entry.getProvider().get();
        task.getConfiguration().getMap().putAll(taskConfiguration.getMap());
        return task;
      }
    }
    throw new IllegalArgumentException("No Task type \'" + taskConfiguration.getType() + "\' found");
  }
}

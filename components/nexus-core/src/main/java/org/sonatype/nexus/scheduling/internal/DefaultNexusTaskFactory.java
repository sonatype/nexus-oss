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

import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.eclipse.sisu.BeanEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link NexusTaskFactory}
 */
@Singleton
@Named
public class DefaultNexusTaskFactory
    extends ComponentSupport
    implements NexusTaskFactory
{
  private final Iterable<BeanEntry<Named, NexusTask>> tasks;

  @Inject
  public DefaultNexusTaskFactory(final Iterable<BeanEntry<Named, NexusTask>> tasks) {
    this.tasks = checkNotNull(tasks);
  }

  @Override
  public <T> T createTaskInstance(final Class<T> taskType) throws IllegalArgumentException {
    log.debug("Creating task by class: {}", taskType.getName());
    return lookupTaskByImplementation(taskType);
  }

  @Override
  public NexusTask<?> createTaskInstanceByFQCN(final String taskFQCN) throws IllegalArgumentException {
    log.debug("Creating task by FQCN: {}", taskFQCN);
    return lookupTaskByImplementationFQCN(taskFQCN);
  }

  @Override
  public NexusTask<?> createTaskInstance(final String taskType) throws IllegalArgumentException {
    log.debug("Creating task by hint: {}", taskType);
    return lookupTaskByHint(taskType);
  }

  // ==

  private <T> T lookupTaskByImplementation(final Class<T> taskType) {
    for (BeanEntry<Named, NexusTask> entry : tasks) {
      if (entry.getImplementationClass().equals(taskType)) {
        return (T) entry.getProvider().get();
      }
    }
    throw new IllegalArgumentException("No Task of type \'" + taskType.getName() + "\' found");
  }

  private NexusTask<?> lookupTaskByImplementationFQCN(final String taskFQCN) {
    for (BeanEntry<Named, NexusTask> entry : tasks) {
      if (entry.getImplementationClass().getName().equals(taskFQCN)) {
        return entry.getProvider().get();
      }
    }
    throw new IllegalArgumentException("No Task having FQCN \'" + taskFQCN + "\' found");
  }

  private NexusTask<?> lookupTaskByHint(final String taskType) {
    for (BeanEntry<Named, NexusTask> entry : tasks) {
      if (entry.getKey().value().equals(taskType)) {
        return entry.getProvider().get();
      }
    }
    throw new IllegalArgumentException("No Task with hint \'" + taskType + "\' found");
  }
}

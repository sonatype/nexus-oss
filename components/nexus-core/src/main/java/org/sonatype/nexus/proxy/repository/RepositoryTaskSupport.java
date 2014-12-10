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
package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Support for {@link Task} implementations that operate on repositories.
 *
 * @since 3.0
 */
public abstract class RepositoryTaskSupport<T>
    extends TaskSupport<T>
{
  private RepositoryRegistry repositoryRegistry;

  @Inject
  public void setRepositoryRegistry(final RepositoryRegistry repositoryRegistry) {
    this.repositoryRegistry = repositoryRegistry;
  }

  public RepositoryRegistry getRepositoryRegistry() {
    return repositoryRegistry;
  }

  /**
   * Returns running tasks having same type as this task, that are running on set of repositories that would
   * overlap with this tasks' processed repositories.
   */
  @Override
  public List<TaskInfo<?>> isBlockedBy(final List<TaskInfo<?>> runningTasks) {
    // we need to do stuff if super says is blocked (task with same type already running)
    // blocked if:
    // me: all-repository => blockedBy all of same type
    // me: X repository => blockedBy other task if runs on same repository or all repositories
    final List<TaskInfo<?>> blockedBy = super.isBlockedBy(runningTasks);
    if (blockedBy.isEmpty()) {
      return blockedBy;
    }
    else if (getConfiguration().getRepositoryId() == null) {
      // am gonna work on all reposes, so am conflicting will all already running ones
      return blockedBy;
    }
    else {
      // am gonna work on X, am blocked if any other running task works on X or all repositories
      return Lists.newArrayList(Iterables.filter(blockedBy, new Predicate<TaskInfo<?>>()
      {
        @Override
        public boolean apply(final TaskInfo<?> taskInfo) {
          return taskInfo.getConfiguration().getRepositoryId() == null
              || getConfiguration().getRepositoryId().equals(taskInfo.getConfiguration().getRepositoryId());
        }
      }));
    }
  }
}

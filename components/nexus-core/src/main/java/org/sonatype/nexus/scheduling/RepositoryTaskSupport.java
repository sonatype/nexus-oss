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
package org.sonatype.nexus.scheduling;

import java.util.List;

import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;

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
   * Returns {@code true} if task having same type as this task already runs, and the running task operates on
   * same set of repositories that this task would.
   */
  @Override
  public boolean isBlocked(final List<TaskInfo<?>> runningTasks) {
    // we need to do stuff if super says is blocked (task with same type already running)
    if (super.isBlocked(runningTasks)) {
      // refine filtering: among these typed tasks, is any of them operating on same set of repositories as me?
      // me: all-repository => blocked
      // me: X repository => blocked if other task runs on same or all-repositories
      if (getConfiguration().getRepositoryId() == null) {
        // am gonna work on all reposes
        return true;
      }
      // filter running ones to my type
      final List<TaskInfo<?>> runningTasksThisType = newArrayList(
          filter(runningTasks, new Predicate<TaskInfo>()
          {
            @Override
            public boolean apply(final TaskInfo input) {
              return getConfiguration().getType().equals(input.getConfiguration().getType());
            }
          }));
      // collect repoIds of filtered tasks
      final List<String> runningTasksThisTypeRepositories = transform(
          runningTasksThisType, new Function<TaskInfo<?>, String>()
          {
            @Override
            public String apply(final TaskInfo<?> input) {
              final String repoId = input.getConfiguration().getRepositoryId();
              if (Strings.isNullOrEmpty(repoId)) {
                return "*";
              }
              else {
                return repoId;
              }
            }
          });
      // some other works on ALL or my repoId
      return runningTasksThisTypeRepositories.contains("*") ||
          runningTasksThisTypeRepositories.contains(getConfiguration().getRepositoryId());
    }
    else {
      // no task with this type running, go ahead
      return false;
    }
  }
}

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

import org.sonatype.nexus.scheduling.CancelableSupport.CancelableFlagHolder;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.MDC;

/**
 * Support for {@link Task} implementations. Subclasses may implement {@link Cancelable} interface if they are
 * implemented to periodically check for {@link #isCanceled()} or {@link CancelableSupport#checkCancellation()}
 * methods. Task implementations should be {@code @Named} components but must not be {@code @Singletons}.
 *
 * @since 3.0
 */
public abstract class TaskSupport<T>
    extends ComponentSupport
    implements Task<T>
{
  private final TaskConfiguration configuration;

  private final CancelableFlagHolder cancelableFlagHolder;

  public TaskSupport() {
    this.configuration = createTaskConfiguration();
    this.cancelableFlagHolder = new CancelableFlagHolder();
  }

  protected TaskConfiguration createTaskConfiguration() {
    return new TaskConfiguration();
  }

  // == NexusTask

  @Override
  public TaskConfiguration getConfiguration() { return configuration; }

  @Override
  public String getId() {
    return getConfiguration().getId();
  }

  @Override
  public String getName() {
    return getConfiguration().getName();
  }

  /**
   * Returns {@code true} if task having same type as this task already runs.
   */
  @Override
  public boolean isBlocked(final List<TaskInfo<?>> runningTasks) {
    final List<String> runningTaskTypes = Lists.transform(runningTasks, new Function<TaskInfo<?>, String>()
    {
      @Override
      public String apply(final TaskInfo<?> input) {
        return input.getConfiguration().getType();
      }
    });
    // same type tasks are blocked
    return runningTaskTypes.contains(getConfiguration().getType());
  }

  @Override
  public final T call() throws Exception {
    MDC.put(TaskSupport.class.getSimpleName(), getClass().getSimpleName());
    CancelableSupport.setCurrent(cancelableFlagHolder);
    try {
      getConfiguration().setMessage(getMessage());
      return execute();
    }
    finally {
      CancelableSupport.setCurrent(null);
      MDC.remove(TaskSupport.class.getSimpleName());
    }
  }

  // == Cancelable (default implementations, used when Cancelable iface implemented)

  public void cancel() {
    cancelableFlagHolder.cancel();
  }

  public boolean isCanceled() {
    return cancelableFlagHolder.isCanceled();
  }

  // == Internal

  /**
   * Where the job is done.
   */
  protected abstract T execute() throws Exception;

  /**
   * Returns short message of current task's instance work. This message is based on task configuration and same
   * typed tasks might emit different messages depending on configuration. Example: "Emptying trash of
   * repository Foo".
   */
  protected abstract String getMessage();

  // ==

  @Override
  public String toString() {
    return String.format("%s(id=%s, name=%s)", getClass().getSimpleName(), getId(), getName());
  }
}

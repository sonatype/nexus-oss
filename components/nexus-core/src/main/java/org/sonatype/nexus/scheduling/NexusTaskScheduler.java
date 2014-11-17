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
import java.util.concurrent.Future;

import org.sonatype.nexus.scheduling.schedule.Schedule;

/**
 * Executor facade component of Nexus, responsible for task executing and scheduling.
 */
public interface NexusTaskScheduler
{
  /**
   * A factory for task configurations (by actual type). It will honor descriptor if exists for given type, otherwise
   * will use sane default values.
   */
  TaskConfiguration createTaskConfigurationInstance(Class<? extends Task> taskType)
      throws IllegalArgumentException;

  /**
   * A factory for task configurations (by FQCN as string). It will honor descriptor if exists for given type, otherwise
   * will use sane default values. It also uses "uber" classloader to load the class, and will check is the class
   * actually a {@link Task}. Use of this method is discouraged, only in cases when "type" of task is geting over
   * wire like in case of UI.
   * // TODO: really? why not enapsulate uberCL here, and only here? Is uberCL "okay" to use or is going away?
   */
  TaskConfiguration createTaskConfigurationInstance(String taskType)
      throws IllegalArgumentException;

  /**
   * A factory for tasks (by actual type). Delegates to {@link NexusTaskFactory}. This method should be rarely used, as
   * it will return a "live" configured task instance that is not scheduled! To be used in cases when task as-is
   * should be executed synchronously, in caller thread using {@link Task#call()} method directly.
   */
  <T extends Task> T createTaskInstance(TaskConfiguration taskConfiguration)
      throws IllegalArgumentException;

  /**
   * Issues a NexusTask for immediate execution, giving control over it with returned {@link Future} instance. Tasks
   * executed via this method are executed as soon as possible, and are not persisted.
   */
  <T> TaskInfo<T> submit(TaskConfiguration configuration);

  /**
   * Returns the {@link TaskInfo<T>} of a task by it's ID, if present, otherwise {@code null}.
   */
  <T> TaskInfo<T> getTaskById(String id);

  /**
   * List existing tasks.
   */
  List<TaskInfo<?>> listsTasks();

  /**
   * Schedules a tasks. If existing task with ID exists, it will be re-scheduled (replaced).
   */
  <T> TaskInfo<T> scheduleTask(TaskConfiguration configuration, Schedule schedule);

  /**
   * Removes task by ID.
   */
  boolean removeTask(String id);

  // -- tests

  // TODO: remove, used in UTs only
  @Deprecated
  void killAll();

  int getRunningTaskCount();
}

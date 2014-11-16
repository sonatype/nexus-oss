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
import java.util.concurrent.Callable;

/**
 * The main interface for all Tasks used in Nexus. All implementations should keep their configuration (if any) in the
 * corresponding {@link TaskConfiguration} object, due to persistence implications. Or, the task might source it's
 * configuration from other (injected) component.
 *
 * @since 3.0
 */
public interface Task<T>
    extends Callable<T>
{
  /**
   * Returns the configuration of the task.
   */
  TaskConfiguration getConfiguration();

  /**
   * Returns a unique ID of the task instance. Shorthand method for {@link #getConfiguration()#getId()}
   */
  String getId();

  /**
   * Returns a descriptive name of the task instance. Shorthand method for {@link #getConfiguration()#getName()}. This
   * method returns always same string for same typed tasks, and it describes what task is about. Example: "Empty
   * trash"
   */
  String getName();

  /**
   * Method should return {@code true} if this task instance should not be run along the other, already running tasks.
   * This method might be invoked multiple times during task instance lifetime but only before it's main execute
   * method. Once the method returns {@code false} (is not blocked), the task execution will continue and this method
   * will not be invoked anymore.
   * // TODO: this should be not exposed via Task iface, this is internal to taskSupport?
   */
  boolean isBlocked(List<TaskInfo<?>> runningTasks);
}

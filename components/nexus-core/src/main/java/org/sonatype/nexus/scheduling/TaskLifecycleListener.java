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

import org.sonatype.sisu.goodies.common.ComponentSupport;

/**
 * Global listener for task lifecycle.
 *
 * @since 3.0
 */
public interface TaskLifecycleListener
{
  /**
   * Invoked when task is about to be executed.
   */
  void onTaskStarted(TaskInfo<?> task);

  /**
   * Invoked when task is cancelled.
   */
  void onTaskCanceled(TaskInfo<?> task);

  /**
   * Invoked when task is done without cancellation or error.
   */
  void onTaskStoppedStopped(TaskInfo<?> task);

  /**
   * Invoked when task is done with cancellation.
   */
  void onTaskStoppedStoppedCanceled(TaskInfo<?> task);

  /**
   * Invoked when task is done with error.
   */
  void onTaskStoppedStoppedFailed(TaskInfo<?> task, Throwable reason);
}

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

import java.util.concurrent.Future;

import com.google.common.annotations.VisibleForTesting;

/**
 * Scheduler facade component of Nexus, responsible for task scheduling. This is like a "SPI" pattern,
 * as actual scheduling might (and probably is) provided by some plugin. You can look at this component
 * as some "simplified" short-hand component to quickly issue background jobs.
 */
public interface NexusScheduler
{
  /**
   * Issues a NexusTask for immediate execution, giving control over it with returned {@link Future} instance.
   */
  <T> Future<T> submit(String name, NexusTask<T> nexusTask);

  /**
   * Returns the count of currently running tasks. To be used only as advisory value, like in tests.
   */
  int getRunningTaskCount();

  /**
   * Kills all running tasks if possible.
   */
  @VisibleForTesting
  void killAll();

  /**
   * A factory for tasks (by actual type).
   */
  <T> T createTaskInstance(Class<T> taskType)
      throws IllegalArgumentException;

  /**
   * A factory for tasks (by FQCN).
   */
  NexusTask<?> createTaskInstanceByFQCN(String taskFQCN)
      throws IllegalArgumentException;
}

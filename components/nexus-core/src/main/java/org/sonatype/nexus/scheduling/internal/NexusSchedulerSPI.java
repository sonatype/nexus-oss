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

import java.util.concurrent.Future;

import org.sonatype.nexus.scheduling.NexusTask;

public interface NexusSchedulerSPI
{
  /**
   * Submits a task for immediate execution. Returns {@link Future} to be able to cancel or wait for the result when
   * task is done.
   */
  <T> Future<T> submit(NexusTask<T> nexusTask);

  /**
   * Returns the count of currently running tasks. To be used only as advisory value, like in tests.
   */
  int getRunningTaskCount();

  /**
   * Kills all running tasks if possible.
   */
  void killAll();
}

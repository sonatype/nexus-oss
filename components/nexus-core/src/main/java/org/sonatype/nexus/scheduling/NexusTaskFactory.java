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

/**
 * The factory for {@link NexusTask} instances.
 */
public interface NexusTaskFactory
{
  /**
   * A factory for tasks.
   */
  <T> T createTaskInstance(Class<T> taskType)
      throws IllegalArgumentException;

  /**
   * A factory for tasks (by FQCN as string). This is internal method, should not be used!
   */
  NexusTask<?> createTaskInstanceByFQCN(String taskFQCN)
      throws IllegalArgumentException;

  /**
   * A factory for tasks. This is the "old fashioned" lookup, that uses "role hint" of the component.
   */
  @Deprecated
  NexusTask<?> createTaskInstance(String taskType)
      throws IllegalArgumentException;
}

/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.appcontext.lifecycle;

/**
 * App context lifecycle manager is very simplistic utility allowing any app context user to implement it's own
 * lifecycle. What triggers it, and what those are are left to implementors.
 *
 * @author cstamas
 * @since 3.1
 */
public interface AppContextLifecycleManager
{
  /**
   * Method to register a handler.
   */
  void registerManaged(LifecycleHandler handler);

  /**
   * Method to unregister a handler.
   */
  void unregisterManaged(LifecycleHandler handler);

  /**
   * Invoke registered handlers for passed in class.
   */
  void invokeHandler(Class<? extends LifecycleHandler> clazz);
}

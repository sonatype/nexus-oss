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

package org.sonatype.sisu.jetty.thread;

/**
 * A simple wrapper that simply "detects" did a wrapped runnable "died" or exited cleanly. Once death detected, the
 * flag
 * remains set.
 *
 * @author cstamas
 * @since 1.3
 */
public class RunnableWrapper
    implements Runnable
{
  private final Runnable runnable;

  static boolean unexpectedThrowable = false;

  public RunnableWrapper(final Runnable runnable) {
    this.runnable = runnable;
  }

  public void run() {
    try {
      runnable.run();
    }
    catch (Throwable e) {
      unexpectedThrowable = true;
    }
  }
}

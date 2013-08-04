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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * Simple thread factory implementation that puts Jetty pooled threads into it's own group and installs
 * {@link UncaughtExceptionHandler} to created threads.
 *
 * @author cstamas
 * @since 1.3
 */
public class ThreadFactoryImpl
    implements ThreadFactory
{
  private final ThreadGroup group;

  private final UncaughtExceptionHandler uncaughtExceptionHandler;

  public ThreadFactoryImpl() {
    this(new ThreadGroup("Jetty8"), new LoggingUncaughtExceptionHandler());
  }

  public ThreadFactoryImpl(final ThreadGroup group, final UncaughtExceptionHandler uncaughtExceptionHandler) {
    this.group = group;
    this.uncaughtExceptionHandler = uncaughtExceptionHandler;
  }

  public Thread newThread(final Runnable runnable) {
    final Thread t = new Thread(group, runnable);
    t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
    return t;
  }
}

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

package org.sonatype.sisu.jetty;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.sonatype.appcontext.AppContext;
import org.sonatype.sisu.jetty.mangler.ContextAttributeSetterMangler;
import org.sonatype.sisu.jetty.mangler.ServerMangler;
import org.sonatype.sisu.jetty.mangler.UnavailableOnStartupExceptionContextMangler;
import org.sonatype.sisu.jetty.util.JettyUtils;

import org.eclipse.jetty.server.Server;

/**
 * A simple component managing embedded Jetty life cycle, configuring Jetty using passed in jetty.xml File,
 * interpolating it with passed in context maps, if any. Usable in cases like UT/ITs, when you can lookup this
 * component
 * and control the Jetty instance with it.
 *
 * @author cstamas
 */
public class Jetty8
{
  private final Server server;

  private final File jettyXml;

  private final ClassLoader classloader;

  private final AppContext appContext;

  private volatile JettyWrapperThread serverThread;

  public Jetty8(final File jettyXml)
      throws IOException
  {
    this(jettyXml, null);
  }

  public Jetty8(final File jettyXml, final AppContext parent)
      throws IOException
  {
    this(jettyXml, Thread.currentThread().getContextClassLoader(), parent);
  }

  public Jetty8(final File jettyXml, final AppContext parent, final Map<?, ?>... overrides)
      throws IOException
  {
    this(jettyXml, Thread.currentThread().getContextClassLoader(), parent, overrides);
  }

  public Jetty8(final File jettyXml, final ClassLoader classloader, final AppContext parent,
                final Map<?, ?>... overrides)
      throws IOException
  {
    this.jettyXml = jettyXml;
    this.classloader = classloader;
    this.server = new Server();
    this.appContext = JettyUtils.configureServer(this.server, this.jettyXml, parent, overrides);
    mangleServer(new ContextAttributeSetterMangler(AppContext.APPCONTEXT_KEY, appContext));
    mangleServer(new UnavailableOnStartupExceptionContextMangler());
  }

  public AppContext getAppContext() {
    return appContext;
  }

  public synchronized boolean isStarted() {
    return serverThread != null;
  }

  public synchronized boolean isRunning() {
    return serverThread != null && serverThread.isAlive() && server.isStarted();
  }

  public synchronized void startJetty()
      throws Exception
  {
    if (isStarted()) {
      throw new IllegalStateException("Jetty already started, stop if first before starting again!");
    }

    serverThread = new JettyWrapperThread(server);
    serverThread.setContextClassLoader(classloader);
    serverThread.startJetty();
  }

  public synchronized void stopJetty()
      throws Exception
  {
    if (!isStarted()) {
      throw new IllegalStateException("Jetty not started, start if first before stopping!");
    }

    serverThread.stopJetty();
    serverThread = null;
  }

  public synchronized <T> T mangleServer(final ServerMangler<T> mangler) {
    return mangler.mangle(server);
  }

  // ==

  public static class JettyWrapperThread
      extends Thread
  {
    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger(1);

    private final Server server;

    private final CountDownLatch started;

    private final CountDownLatch stopped;

    private volatile Exception exception;

    public JettyWrapperThread(final Server server) {
      super("jetty-main-thread-" + INSTANCE_COUNTER.getAndIncrement());
      this.server = server;
      this.started = new CountDownLatch(1);
      this.stopped = new CountDownLatch(1);
    }

    @Override
    public void run() {
      try {
        try {
          server.start();
        }
        catch (Exception e) {
          exception = e;
        }
        finally {
          started.countDown();
        }

        server.join();
      }
      catch (InterruptedException e) {
        // nothing
      }
      finally {
        stopped.countDown();
      }
    }

    public void startJetty()
        throws Exception
    {
      start();
      started.await();

      if (exception != null) {
        throw exception;
      }
    }

    public void stopJetty()
        throws Exception
    {
      final ClassLoader original = Thread.currentThread().getContextClassLoader();

      try {
        Thread.currentThread().setContextClassLoader(getContextClassLoader());
        server.stop();
        stopped.await();

        if (exception != null) {
          throw exception;
        }
      }
      finally {
        Thread.currentThread().setContextClassLoader(original);
      }
    }
  }
}

/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.bootstrap.osgi;

import org.sonatype.nexus.bootstrap.Launcher;
import org.sonatype.nexus.bootstrap.ShutdownHelper;
import org.sonatype.nexus.bootstrap.ShutdownHelper.ShutdownDelegate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.slf4j.MDC;

import static org.sonatype.nexus.bootstrap.Launcher.SYSTEM_USERID;

/**
 * {@link BundleActivator} that invokes the {@link Launcher}.
 * 
 * @since 3.0
 */
public class LauncherActivator
    implements BundleActivator, ShutdownDelegate
{
  private Framework framework;

  private Launcher server;

  public void start(BundleContext bundleContext) throws Exception {
    framework = (Framework) bundleContext.getBundle(0);
    ShutdownHelper.setDelegate(this);

    final String basePath = bundleContext.getProperty("nexus-base");
    final String args = bundleContext.getProperty("nexus-args");

    MDC.put("userId", SYSTEM_USERID);
    server = new Launcher(basePath, null, null, args.split(","));
    server.startAsync();
  }

  public void stop(BundleContext bundleContext) throws Exception {
    try {
      if (server != null) {
        server.stop();
      }
    }
    finally {
      server = null;
    }
  }

  public void doExit(int code) {
    try {
      framework.stop();
      framework.waitForStop(0);
    }
    catch (InterruptedException e) {
      // proceed to exit
    }
    catch (Throwable e) {
      e.printStackTrace();
    }
    finally {
      System.exit(code);
    }
  }

  public void doHalt(int code) {
    Runtime.getRuntime().halt(code);
  }
}

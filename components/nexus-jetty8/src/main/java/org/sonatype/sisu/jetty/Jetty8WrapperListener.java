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

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * Class extending Jetty8 and adding Java Service Wrapper support on it, making Jetty "jsw-ized". This class is used as
 * "main" launched class in Nexus bundle for example. Requirement for App is to have path to jetty.xml passed as first
 * argument. Just make this one a "main class" in JSW config and pass in the path of jetty.xml as 1st parameter and you
 * are done.
 *
 * @author cstamas
 */
public class Jetty8WrapperListener
    extends Jetty8
    implements WrapperListener
{
  protected static final Object waitObj = new Object();

  protected Jetty8WrapperListener(final File jettyXml)
      throws IOException
  {
    // nope, do not instantiate this directly, just from main()!
    super(jettyXml);
  }

  // WrapperListener
  public Integer start(final String[] args) {
    WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Starting Jetty...");

    try {
      startJetty();

      return null;
    }
    catch (Exception e) {
      WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_FATAL, "Unable to start Jetty: " + e.getMessage());

      e.printStackTrace();

      return 1;
    }
  }

  // WrapperListener
  public int stop(final int exitCode) {
    WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_INFO, "Stopping Jetty...");

    try {
      stopJetty();

      return exitCode;
    }
    catch (Exception e) {
      WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_FATAL,
          "Unable to stop Jetty cleanly: " + e.getMessage());

      e.printStackTrace();

      return 1;
    }
    finally {
      synchronized (waitObj) {
        waitObj.notify();
      }
    }
  }

  // WrapperListener
  public void controlEvent(final int event) {
    if ((event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) && WrapperManager.isLaunchedAsService()) {
      // Ignore this event, it's just user logged out and we are a service, so continue running
      if (WrapperManager.isDebugEnabled()) {
        WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "Jetty8WrapperListener: controlEvent("
            + event + ") Ignored");
      }
    }
    else {
      if (WrapperManager.isDebugEnabled()) {
        WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_DEBUG, "Jetty8WrapperListener: controlEvent("
            + event + ") Stopping");
      }

      WrapperManager.stop(0);
      // Will not get here.
    }
  }

  // ==

  /**
   * "Standard" main method, starts embedded Jetty "by the book", and it returns.
   */
  public static void main(final String[] args)
      throws IOException
  {
    if (args != null && args.length > 0) {
      final File jettyXml = new File(args[0]);

      WrapperManager.start(new Jetty8WrapperListener(jettyXml), args);
    }
    else {
      WrapperManager.log(WrapperManager.WRAPPER_LOG_LEVEL_FATAL,
          "First supplied app parameter should be path to existing Jetty8 XML configuration file!");

      WrapperManager.stop(1);
    }
  }
}

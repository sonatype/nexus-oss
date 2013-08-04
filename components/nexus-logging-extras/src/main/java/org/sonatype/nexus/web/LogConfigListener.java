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

package org.sonatype.nexus.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.log.LogManager;

import com.google.common.base.Throwables;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.google.common.base.Preconditions.checkState;

/**
 * Initialize logging system on start-up.
 *
 * @author juven
 * @author adreghiciu@gmail.com
 */
public class LogConfigListener
    implements ServletContextListener
{
  public void contextInitialized(ServletContextEvent event) {
    // FIXME: JUL handler should be handled by container or bootstrap
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    configureLogManager(event.getServletContext());
  }

  public void contextDestroyed(ServletContextEvent event) {
    // ignore
  }

  private void configureLogManager(ServletContext context) {
    try {
      // FIXME: Replace with Guice-based lookup of component
      PlexusContainer container = (PlexusContainer) context.getAttribute(PlexusConstants.PLEXUS_KEY);
      checkState(container != null, "Could not find Plexus container in servlet context");
      LogManager logManager = container.lookup(LogManager.class);
      logManager.configure();
    }
    catch (ComponentLookupException e) {
      throw Throwables.propagate(e);
    }
  }
}

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

import org.sonatype.nexus.NxApplication;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;

/**
 * This J2EE ServletContextListener gets the Plexus from servlet context, and uses it to lookup, hence "boot" the Nexus
 * component.
 */
public class NexusBooterListener
    implements ServletContextListener
{
  @Override
  public void contextInitialized(ServletContextEvent event) {
    final ServletContext context = event.getServletContext();
    try {
      final PlexusContainer plexus = (PlexusContainer) context.getAttribute(PlexusConstants.PLEXUS_KEY);
      plexus.lookup(NxApplication.class).start();
    }
    catch (Exception e) {
      throw new IllegalStateException("Could not start Nexus", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    final ServletContext context = event.getServletContext();
    try {
      final PlexusContainer plexus = (PlexusContainer) context.getAttribute(PlexusConstants.PLEXUS_KEY);
      plexus.lookup(NxApplication.class).stop();
    }
    catch (Exception e) {
      throw new IllegalStateException("Could not stop Nexus", e);
    }
  }
}

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

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Initializes guice servlet extension {@see PlexusContainerContextListener#getInjector()}.
 *
 * @author adreghiciu
 */
public class NexusGuiceContextListener
    extends GuiceServletContextListener
{

  private ServletContext servletContext;

  public void contextInitialized(ServletContextEvent sce) {
    servletContext = sce.getServletContext();
    super.contextInitialized(sce);
  }

  @Override
  protected Injector getInjector() {
    try {
      PlexusContainer plexusContainer =
          (PlexusContainer) servletContext.getAttribute(PlexusConstants.PLEXUS_KEY);
      return plexusContainer.lookup(Injector.class);
    }
    catch (ComponentLookupException e) {
      throw new IllegalStateException("Could not locate Guice Injector.", e);
    }
  }

}

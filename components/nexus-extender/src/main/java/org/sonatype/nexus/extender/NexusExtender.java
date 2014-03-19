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
package org.sonatype.nexus.extender;

import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContext;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.guice.NexusModules.CoreModule;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.web.internal.NexusGuiceFilter;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.eclipse.sisu.inject.MutableBeanLocator;
import org.eclipse.sisu.launch.SisuExtender;
import org.eclipse.sisu.plexus.PlexusSpaceModule;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 3.0
 */
public class NexusExtender
    extends SisuExtender
{
  private static final Logger log = LoggerFactory.getLogger(NexusExtender.class);

  private ServletContext servletContext;

  private Injector injector;

  // private ServletContextListener listener;

  private PlexusContainer container;

  private LogManager logManager;

  private NxApplication application;

  @Override
  protected MutableBeanLocator createLocator(final BundleContext ctx) {

    // TODO: clean-up transfer of surrounding context
    final Map<?, ?> properties = System.getProperties();
    final Bundle systemBundle = ctx.getBundle(0);

    servletContext = ctx.getService(ctx.getServiceReference(ServletContext.class));

    final ClassSpace coreSpace = new BundleClassSpace(ctx.getBundle());
    injector = Guice.createInjector(
        new WireModule(
            new CoreModule(servletContext, properties, systemBundle),
            new PlexusSpaceModule(coreSpace, BeanScanning.GLOBAL_INDEX)));
    log.debug("Injector: {}", injector);

    // listener = new GuiceServletContextListener()
    // {
    // @Override
    // protected Injector getInjector() {
    // return injector;
    // }
    // };
    // listener.contextInitialized(new ServletContextEvent(servletContext));

    return injector.getInstance(MutableBeanLocator.class);
  }

  @Override
  public void start(BundleContext ctx) {
    super.start(ctx);

    container = injector.getInstance(PlexusContainer.class);
    servletContext.setAttribute(PlexusConstants.PLEXUS_KEY, container);
    injector.getInstance(Context.class).put(PlexusConstants.PLEXUS_KEY, container);
    log.debug("Container: {}", container);

    try {
      logManager = container.lookup(LogManager.class);
      log.debug("Log manager: {}", logManager);
      logManager.configure();

      application = container.lookup(NxApplication.class);
      log.debug("Application: {}", application);
      application.start();
    }
    catch (final Exception e) {
      log.error("Failed to start application", e);
      Throwables.propagate(e);
    }

    // pass back our filter instance to the surrounding bootstrap context
    ctx.registerService(Filter.class, injector.getInstance(NexusGuiceFilter.class), null);
  }

  @Override
  public void stop(final BundleContext ctx) {
    super.stop(ctx);

    if (application != null) {
      try {
        application.stop();
      }
      catch (final Exception e) {
        log.error("Failed to stop application", e);
      }
      application = null;
    }

    if (logManager != null) {
      logManager.shutdown();
      logManager = null;
    }

    if (container != null) {
      container.dispose();
      container = null;
    }

    if (servletContext != null) {
      // if (listener != null) {
      // listener.contextDestroyed(new ServletContextEvent(servletContext));
      // listener = null;
      // }
      servletContext.removeAttribute(PlexusConstants.PLEXUS_KEY);
      servletContext = null;
    }

    injector = null;
  }
}
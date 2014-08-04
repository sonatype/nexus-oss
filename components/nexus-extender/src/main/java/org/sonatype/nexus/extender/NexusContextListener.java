/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.web.NexusGuiceFilter;
import org.sonatype.nexus.web.NexusServletModule;

import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceServletContextListener;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonMap;

/**
 * {@link ServletContextListener} that bootstraps the core Nexus application.
 * 
 * @since 3.0
 */
public class NexusContextListener
    extends GuiceServletContextListener
    implements FrameworkListener
{
  private static final int NEXUS_PLUGIN_START_LEVEL = 200;

  private static final Logger log = LoggerFactory.getLogger(NexusContextListener.class);

  private final NexusBundleExtender extender;

  private BundleContext bundleContext;

  private ServletContext servletContext;

  private Injector injector;

  private LogManager logManager;

  private NxApplication application;

  private ServiceRegistration<Filter> registration;

  public NexusContextListener(final NexusBundleExtender extender) {
    this.extender = extender;
  }

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    SharedMetricRegistries.getOrCreate("nexus");

    bundleContext = extender.getBundleContext();

    servletContext = event.getServletContext();
    Map<?, ?> variables = (Map<?, ?>) servletContext.getAttribute("nexus.properties");
    if (variables == null) {
      variables = System.getProperties();
    }

    injector = Guice.createInjector(new WireModule(new NexusServletModule(servletContext, variables)));
    log.debug("Injector: {}", injector);

    super.contextInitialized(event);

    extender.doStart(); // start tracking nexus bundles

    try {
      logManager = lookup(LogManager.class);
      log.debug("Log manager: {}", logManager);
      logManager.configure();

      application = lookup(NxApplication.class);
      log.debug("Application: {}", application);

      log.info("Activating locally installed plugins...");

      final Bundle systemBundle = bundleContext.getBundle(0);

      // raising the start level will activate Nexus plugins - after it's done we get a callback
      systemBundle.adapt(FrameworkStartLevel.class).setStartLevel(NEXUS_PLUGIN_START_LEVEL, this);
    }
    catch (final Exception e) {
      log.error("Failed to lookup application", e);
      Throwables.propagate(e);
    }
  }

  public void frameworkEvent(final FrameworkEvent event) {
    if (event.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
      // any local Nexus plugins have now been activated

      try {
        application.start();
      }
      catch (final Exception e) {
        log.error("Failed to start application", e);
        Throwables.propagate(e);
      }

      // register our dynamic filter with the surrounding bootstrap code
      final Filter filter = injector.getInstance(NexusGuiceFilter.class);
      final Dictionary<String, ?> properties = new Hashtable<>(singletonMap("name", "nexus"));
      registration = bundleContext.registerService(Filter.class, filter, properties);
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {

    // remove our dynamic filter
    if (registration != null) {
      registration.unregister();
      registration = null;
    }

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

    extender.doStop(); // stop tracking bundles

    if (servletContext != null) {
      super.contextDestroyed(new ServletContextEvent(servletContext));
      servletContext = null;
    }

    injector = null;

    SharedMetricRegistries.remove("nexus");
  }

  @Override
  protected Injector getInjector() {
    checkState(injector != null, "Missing injector reference");
    return injector;
  }

  private <T> T lookup(final Class<T> clazz) {
    return injector.getInstance(BeanLocator.class).locate(Key.get(clazz)).iterator().next().getValue();
  }
}

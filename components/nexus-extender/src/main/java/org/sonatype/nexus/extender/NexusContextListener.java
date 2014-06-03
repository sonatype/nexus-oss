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

import java.io.File;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.guice.NexusModules.CoreModule;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.web.internal.NexusGuiceFilter;

import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceServletContextListener;
import org.eclipse.sisu.inject.BeanLocator;
import org.eclipse.sisu.space.BeanScanning;
import org.eclipse.sisu.space.BundleClassSpace;
import org.eclipse.sisu.space.ClassSpace;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.wire.WireModule;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
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
{
  private static final Logger log = LoggerFactory.getLogger(NexusContextListener.class);

  private final NexusBundleExtender extender;

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

    final BundleContext ctx = extender.getBundleContext();

    servletContext = event.getServletContext();
    Map<?, ?> variables = (Map<?, ?>) servletContext.getAttribute("nexus.properties");
    if (variables == null) {
      variables = System.getProperties();
    }

    final Bundle systemBundle = ctx.getBundle(0);

    final ClassSpace coreSpace = new BundleClassSpace(ctx.getBundle());
    injector = Guice.createInjector(
        new WireModule(
            new CoreModule(servletContext, variables, systemBundle),
            new SpaceModule(coreSpace, BeanScanning.GLOBAL_INDEX)));
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

      startNexusPlugins(ctx, new File(variables.get("nexus-app") + "/plugin-repository"));
      startNexusPlugins(ctx, new File(variables.get("nexus-work") + "/plugin-repository"));

      application.start();
    }
    catch (final Exception e) {
      log.error("Failed to start application", e);
      Throwables.propagate(e);
    }

    // register our dynamic filter with the surrounding bootstrap code
    final Filter filter = injector.getInstance(NexusGuiceFilter.class);
    final Dictionary<String, ?> properties = new Hashtable<>(singletonMap("name", "nexus"));
    registration = ctx.registerService(Filter.class, filter, properties);
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

  private static void startNexusPlugins(final BundleContext ctx, File pluginRepository) {
    File[] pluginFiles = pluginRepository.listFiles();
    if (pluginFiles != null && pluginFiles.length > 0) {
      List<Bundle> plugins = new ArrayList<>();
      for (File file : pluginFiles) {
        try {
          plugins.add(ctx.installBundle("reference:" + file.toURI()));
        }
        catch (Exception e) {
          log.warn("Problem installing: {}", file, e);
        }
      }
      for (Bundle plugin : plugins) {
        try {
          plugin.start();
        }
        catch (Exception e) {
          log.warn("Problem starting: {}", plugin, e);
        }
      }
    }
  }
}

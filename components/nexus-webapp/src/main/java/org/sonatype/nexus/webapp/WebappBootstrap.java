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

package org.sonatype.nexus.webapp;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.NxApplication;
import org.sonatype.nexus.bootstrap.ConfigurationBuilder;
import org.sonatype.nexus.bootstrap.ConfigurationHolder;
import org.sonatype.nexus.guice.NexusModules.CoreModule;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.util.LockFile;
import org.sonatype.nexus.web.NexusWebModule;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static com.google.common.base.Preconditions.checkState;

/**
 * Web application bootstrap {@link ServletContextListener}.
 *
 * @since 2.8
 */
public class WebappBootstrap
    extends GuiceServletContextListener
{
  private static final Logger log = LoggerFactory.getLogger(WebappBootstrap.class);

  private static final String CUSTOM_MODULES = "customModules";

  private LockFile lockFile;

  private PlexusContainer container;

  private Injector injector;

  private NxApplication application;

  private LogManager logManager;

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    log.info("Initializing");

    ServletContext context = event.getServletContext();

    // FIXME: Why is this here?  for legacy test shit?
    if (context.getAttribute(PlexusConstants.PLEXUS_KEY) != null) {
      log.info("Plexus container already exists; skipping");
      return;
    }

    // FIXME: JUL handler should be handled by container or bootstrap
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();

    try {
      // Use bootstrap configuration if it exists, else load it
      Map<String, String> properties = ConfigurationHolder.get();
      if (properties != null) {
        log.info("Using bootstrap launcher configuration");
      }
      else {
        log.info("Loading configuration for WAR deployment environment");

        // FIXME: This is what was done before, it seems completly wrong in WAR deployment since there is no bundle
        String baseDir = System.getProperty("bundleBasedir", context.getRealPath("/WEB-INF"));

        properties = new ConfigurationBuilder()
            .defaults()
            .set("bundleBasedir", new File(baseDir).getCanonicalPath())
            .properties("/nexus.properties", true)
            .properties("/nexus-test.properties", false)
            .build();

        System.getProperties().putAll(properties);
        ConfigurationHolder.set(properties);
      }

      // Ensure required properties exist
      requireProperty(properties, "bundleBasedir");
      requireProperty(properties, "nexus-work");
      requireProperty(properties, "nexus-app");
      requireProperty(properties, "application-conf");
      requireProperty(properties, "security-xml-file");

      // lock the work directory
      File workDir = new File(properties.get("nexus-work")).getCanonicalFile();
      lockFile = new LockFile(new File(workDir, "nexus.lock"));
      checkState(lockFile.lock(), "Nexus work directory already in use: %s", workDir);

      // setup plexus configuration
      URL plexusXml = getClass().getResource("/plexus.xml");
      checkState(plexusXml != null, "Missing plexus.xml");
      log.debug("Plexus XML: {}", plexusXml);

      @SuppressWarnings("unchecked")
      ContainerConfiguration plexusConfiguration = new DefaultContainerConfiguration()
          .setName(context.getServletContextName())
          .setContainerConfigurationURL(plexusXml)
          .setContext((Map) properties)
          .setAutoWiring(true)
          .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
          .setComponentVisibility(PlexusConstants.GLOBAL_VISIBILITY);

      List<Module> modules = Lists.newArrayList(
          new NexusWebModule(context),
          new CoreModule()
      );

      // FIXME: What is this used for?
      Module[] customModules = (Module[]) context.getAttribute(CUSTOM_MODULES);
      if (customModules != null) {
        modules.addAll(Arrays.asList(customModules));
      }

      // create the container
      container = new DefaultPlexusContainer(plexusConfiguration, modules.toArray(new Module[modules.size()]));
      context.setAttribute(PlexusConstants.PLEXUS_KEY, container);
      log.debug("Container: {}", container);

      // configure guice servlet
      injector = container.lookup(Injector.class);
      log.debug("Injector: {}", injector);
      super.contextInitialized(event);

      // configure logging
      logManager = container.lookup(LogManager.class);
      log.debug("Log manager: {}", logManager);
      logManager.configure();

      // start the application
      application = container.lookup(NxApplication.class);
      log.debug("Application: {}", application);
      application.start();
    }
    catch (Exception e) {
      log.error("Failed to initialize", e);
      throw Throwables.propagate(e);
    }

    log.info("Initialized");
  }

  private void requireProperty(final Map<String, String> properties, final String name) {
    if (!properties.containsKey(name)) {
      throw new IllegalStateException("Missing required property: " + name);
    }
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    log.info("Destroying");

    // stop application
    if (application != null) {
      try {
        application.stop();
      }
      catch (Exception e) {
        log.error("Failed to stop application", e);
      }
      application = null;
    }

    // shutdown logging
    if (logManager != null) {
      logManager.shutdown();
      logManager = null;
    }

    super.contextDestroyed(event);

    // cleanup the container
    if (container != null) {
      container.dispose();
      container = null;
    }

    // release lock
    if (lockFile != null) {
      lockFile.release();
      lockFile = null;
    }

    log.info("Destroyed");
  }

  @Override
  protected Injector getInjector() {
    checkState(injector != null, "Missing injector reference");
    return injector;
  }
}

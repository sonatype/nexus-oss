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
package org.sonatype.nexus.jettyapp;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.sonatype.nexus.bootstrap.ConfigurationBuilder;
import org.sonatype.nexus.bootstrap.ConfigurationHolder;
import org.sonatype.nexus.bootstrap.EnvironmentVariables;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty application bootstrap {@link BundleActivator}.
 * 
 * @since 3.0
 */
public class JettyAppBootstrap
    implements BundleActivator
{
  private static final Logger log = LoggerFactory.getLogger(JettyAppBootstrap.class);

  private ListenerTracker listenerTracker;

  private FilterTracker filterTracker;

  public void start(BundleContext ctx) throws Exception {
    log.info("Initializing");

    try {
      // Use bootstrap configuration if it exists, else load it
      Map<String, String> properties = ConfigurationHolder.get();
      if (properties != null) {
        log.info("Using bootstrap launcher configuration");
      }
      else {
        log.info("Loading configuration for OSGi environment");

        String baseDir = ctx.getProperty("bundleBasedir");

        properties = new ConfigurationBuilder()
            .defaults()
            .set("bundleBasedir", new File(baseDir).getCanonicalPath())
            .properties("/nexus.properties", true)
            .properties("/nexus-test.properties", false)
            .custom(new EnvironmentVariables())
            .override(System.getProperties())
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

      File workDir = new File(properties.get("nexus-work")).getCanonicalFile();
      mkdir(workDir.toPath());

      WebAppContext handler = new WebAppContext();

      handler.setContextPath("/nexus");
      handler.setDisplayName("Sonatype Nexus");
      handler.setWelcomeFiles(new String[] { "index.html" });

      // expose static content from this bootstrap bundle
      handler.setBaseResource(Resource.newResource(ctx.getBundle().getEntry("static")));

      ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
      errorHandler.addErrorPage(ErrorPageErrorHandler.GLOBAL_ERROR_PAGE, "/error.html");
      handler.setErrorHandler(errorHandler);

      // pass bootstrap properties to embedded servlet listener
      handler.setAttribute("nexus.properties", properties);

      ctx.registerService(ContextHandler.class, handler, null);

      listenerTracker = new ListenerTracker(ctx, "nexus", handler.getServletContext());
      listenerTracker.open();

      filterTracker = new FilterTracker(ctx, "nexus", handler.getServletHandler());
      filterTracker.open();
    }
    catch (Exception e) {
      log.error("Failed to start Nexus", e);
      throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
    }

    log.info("Initialized");
  }

  private static void requireProperty(final Map<String, String> properties, final String name) {
    if (!properties.containsKey(name)) {
      throw new IllegalStateException("Missing required property: " + name);
    }
  }

  private static void mkdir(final Path dir) throws IOException {
    try {
      Files.createDirectories(dir);
    }
    catch (FileAlreadyExistsException e) {
      // this happens when last element of path exists, but is a symlink.
      // A simple test with Files.isDirectory should be able to detect this
      // case as by default, it follows symlinks.
      if (!Files.isDirectory(dir)) {
        throw e;
      }
    }
  }

  public void stop(BundleContext ctx) throws Exception {
    log.info("Destroying");

    if (filterTracker != null) {
      filterTracker.close();
      filterTracker = null;
    }

    if (listenerTracker != null) {
      listenerTracker.close();
      listenerTracker = null;
    }

    log.info("Destroyed");
  }
}

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
package org.sonatype.nexus.bootstrap.osgi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.bootstrap.ConfigurationBuilder;
import org.sonatype.nexus.bootstrap.ConfigurationHolder;
import org.sonatype.nexus.bootstrap.EnvironmentVariables;
import org.sonatype.nexus.bootstrap.LockFile;

import ch.qos.logback.classic.LoggerContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * {@link ServletContextListener} that bootstraps an OSGi-based application.
 * 
 * @since 3.0
 */
public class BootstrapListener
    implements ServletContextListener
{
  private static final Logger log = LoggerFactory.getLogger(BootstrapListener.class);

  private LockFile lockFile;

  private Framework framework;

  private ListenerTracker listenerTracker;

  private FilterTracker filterTracker;

  public void contextInitialized(final ServletContextEvent event) {
    log.info("Initializing");

    ServletContext servletContext = event.getServletContext();

    try {
      // Use bootstrap configuration if it exists, else load it
      Map<String, String> properties = ConfigurationHolder.get();
      if (properties != null) {
        log.info("Using bootstrap launcher configuration");
      }
      else {
        log.info("Loading configuration for WAR deployment");

        String baseDir = (String) servletContext.getAttribute("nexus-base");
        if (baseDir == null) {
          baseDir = servletContext.getRealPath("/WEB-INF");
        }

        properties = new ConfigurationBuilder()
            .defaults()
            .set("nexus-base", new File(baseDir).getCanonicalPath())
            .properties("/nexus.properties", true)
            .properties("/nexus-test.properties", false)
            .custom(new EnvironmentVariables())
            .override(System.getProperties())
            .build();

        System.getProperties().putAll(properties);
        ConfigurationHolder.set(properties);
      }

      // Ensure required properties exist
      requireProperty(properties, "nexus-base");
      requireProperty(properties, "nexus-work");
      requireProperty(properties, "nexus-app");
      requireProperty(properties, "application-conf");
      requireProperty(properties, "security-xml-file");

      // pass bootstrap properties to embedded servlet listener
      servletContext.setAttribute("nexus.properties", properties);

      File workDir = new File(properties.get("nexus-work")).getCanonicalFile();
      mkdir(workDir.toPath());

      // lock the work directory
      lockFile = new LockFile(new File(workDir, "nexus.lock"));
      if (!lockFile.lock()) {
        throw new IllegalStateException("Nexus work directory already in use: " + workDir);
      }

      // are we already running in OSGi or should we embed OSGi?
      Bundle containingBundle = FrameworkUtil.getBundle(getClass());
      BundleContext bundleContext;
      if (containingBundle != null) {
        bundleContext = containingBundle.getBundleContext();
      }
      else {
        bundleContext = launchFramework(workDir, properties);
      }

      // watch out for the real Nexus listener
      listenerTracker = new ListenerTracker(bundleContext, "nexus", servletContext);
      listenerTracker.open();

      // watch out for the real Nexus filter
      filterTracker = new FilterTracker(bundleContext, "nexus");
      filterTracker.open();

      listenerTracker.waitForService(0);
      filterTracker.waitForService(0);
    }
    catch (Exception e) {
      log.error("Failed to start Nexus", e);
      throw e instanceof RuntimeException ? ((RuntimeException) e) : new RuntimeException(e);
    }

    log.info("Initialized");
  }

  private BundleContext launchFramework(File workDir, Map<String, String> properties) throws IOException,
      BundleException
  {
    StringBuilder exports = new StringBuilder("com.sun.net.httpserver,");
    InputStream is = getClass().getResourceAsStream("/BOOT-INF/MANIFEST.MF");
    try {
      exports.append(new Manifest(is).getMainAttributes().getValue(Constants.EXPORT_PACKAGE));
    }
    finally {
      is.close();
    }

    // export additional core (non-plugin) packages from system bundle
    properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exports.toString());

    properties.put(Constants.FRAMEWORK_STORAGE, workDir + "/felix-cache");
    properties.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
    properties.put(Constants.FRAMEWORK_BOOTDELEGATION, "sun.*");

    framework = ServiceLoader.load(FrameworkFactory.class).iterator().next().newFramework(properties);

    framework.init();
    framework.start();

    BundleContext bundleContext = framework.getBundleContext();
    FrameworkStartLevel startLevel = framework.adapt(FrameworkStartLevel.class);
    startLevel.setStartLevel(1);

    LoggerContext loggerContext = (LoggerContext) StaticLoggerBinder.getSingleton().getLoggerFactory();
    loggerContext.putObject("org.ops4j.pax.logging.logback.bundlecontext", bundleContext);

    startLevel.setInitialBundleStartLevel(80);

    File[] bundleFiles = new File(properties.get("nexus-app") + "/bundles").listFiles();

    // auto-install anything in the bundles repository
    if (bundleFiles != null && bundleFiles.length > 0) {
      for (File file : bundleFiles) {
        try {
          Bundle bundle = bundleContext.installBundle("reference:" + file.toURI());
          BundleRevision revision = bundle.adapt(BundleRevision.class);
          if (revision != null && (revision.getTypes() & BundleRevision.TYPE_FRAGMENT) == 0) {
            bundle.start(); // only need to start non-fragment bundles
          }
        }
        catch (Exception e) {
          log.warn("Problem installing: {}", file, e);
        }
      }
    }

    startLevel.setStartLevel(80); // activate all installed bundles
    return bundleContext;
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

  public void contextDestroyed(final ServletContextEvent event) {
    log.info("Destroying");

    if (filterTracker != null) {
      filterTracker.close();
      filterTracker = null;
    }

    if (listenerTracker != null) {
      listenerTracker.close();
      listenerTracker = null;
    }

    if (framework != null) {
      try {
        framework.stop();
        framework.waitForStop(0);
      }
      catch (Exception e) {
        log.error("Failed to stop Nexus", e);
      }
      framework = null;
    }

    if (lockFile != null) {
      lockFile.release();
      lockFile = null;
    }

    log.info("Destroyed");
  }
}

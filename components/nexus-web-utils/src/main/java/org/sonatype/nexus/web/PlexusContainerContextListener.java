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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.sonatype.nexus.guice.NexusModules.CoreModule;
import org.sonatype.nexus.util.LockFile;

import com.google.inject.Module;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * This ServeletContextListener boots up Plexus in a webapp environment, if needed.
 *
 * It is safe to have it multiple times executed, since it will create only once, or reuse the found container.
 *
 * @author cstamas
 */
public class PlexusContainerContextListener
    implements ServletContextListener
{
  private static final Logger log = LoggerFactory.getLogger(PlexusContainerContextListener.class);

  public static final String CUSTOM_MODULES = "customModules";

  private LockFile lockFile;

  private PlexusContainer plexusContainer;

  public void contextInitialized(final ServletContextEvent event) {
    final ServletContext context = event.getServletContext();

    if (context.getAttribute(PlexusConstants.PLEXUS_KEY) != null) {
      log.info("Plexus container already exists; skipping");
      return;
    }

    Map<String, String> properties = new HashMap<>();

    try {
      //final File nexusWorkdir = new File(String.valueOf(appContext.get("nexus-work"))).getCanonicalFile();
      //lockFile = new LockFile(new File(nexusWorkdir, "nexus.lock"));
      //if (!lockFile.lock()) {
      //  throw new IllegalStateException("Nexus work directory already is use!");
      //}

      URL plexusXml = getClass().getResource("/plexus.xml");
      checkState(plexusXml != null, "Missing plexus.xml");

      @SuppressWarnings("unchecked")
      final ContainerConfiguration plexusConfiguration = new DefaultContainerConfiguration()
          .setName(context.getServletContextName())
          .setContainerConfigurationURL(plexusXml)
          .setContext((Map) properties)
          .setAutoWiring(true)
          .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
          .setComponentVisibility(PlexusConstants.GLOBAL_VISIBILITY);

      final List<Module> modules = new ArrayList<>(2);
      modules.add(new NexusWebModule(event.getServletContext()));
      modules.add(new CoreModule());

      final Module[] customModules = (Module[]) context.getAttribute(CUSTOM_MODULES);
      if (customModules != null) {
        modules.addAll(Arrays.asList(customModules));
      }

      plexusContainer = new DefaultPlexusContainer(plexusConfiguration, modules.toArray(new Module[modules.size()]));

      context.setAttribute(PlexusConstants.PLEXUS_KEY, plexusContainer);
    }
    catch (Exception e) {
      context.log("Could not start Plexus container!", e);
      throw new IllegalStateException("Could not start Plexus container!", e);
    }
  }

  public void contextDestroyed(final ServletContextEvent sce) {
    if (plexusContainer != null) {
      plexusContainer.dispose();
    }
    if (lockFile != null) {
      lockFile.release();
    }
  }

  //protected AppContext createContainerContext(final ServletContext context, final AppContext parent)
  //    throws AppContextException, IOException
  //{
  //  if (parent == null) {
  //    context.log("Configuring Nexus in vanilla WAR...");
  //  }
  //  else {
  //    context.log("Configuring Nexus in bundle...");
  //  }
  //
  //  File basedirFile = null;
  //  File warWebInfFile = null;
  //
  //  String baseDirProperty = System.getProperty("bundleBasedir");
  //
  //  if (!StringUtils.isEmpty(baseDirProperty)) {
  //    basedirFile = new File(baseDirProperty).getCanonicalFile();
  //    // Nexus as bundle case
  //    context.log("Setting Plexus basedir context variable to (pre-set in System properties): " +
  //        basedirFile.getAbsolutePath());
  //  }
  //
  //  String warWebInfFilePath = context.getRealPath("/WEB-INF");
  //
  //  if (!StringUtils.isEmpty(warWebInfFilePath)) {
  //    warWebInfFile = new File(warWebInfFilePath).getCanonicalFile();
  //
  //    if (basedirFile == null) {
  //      context.log("Setting Plexus basedir context variable to (discovered from Servlet container): " + warWebInfFile);
  //      basedirFile = warWebInfFile;
  //    }
  //  }
  //  else {
  //    String message = "Could not set Plexus basedir, Nexus cannot run from non-unpacked WAR!";
  //    context.log(message);
  //
  //    throw new IllegalStateException(message);
  //  }
  //
  //  // plexus files are always here
  //  plexusXmlFile = new File(warWebInfFile, "plexus.xml");
  //
  //  // no "real" parenting for now
  //  // for historical reasons, honor the "plexus" prefix too
  //  AppContextRequest request = Factory.getDefaultRequest("nexus", parent, Arrays.asList("plexus"));
  //
  //  // add the user overridable test properties file, but it might not be present
  //  request.getSources()
  //      .add(0, new PropertiesFileEntrySource(new File(basedirFile, "conf/nexus-test.properties"), false));
  //  // add the user overridable properties file, but it might not be present
  //  request.getSources().add(0, new PropertiesFileEntrySource(new File(basedirFile, "conf/nexus.properties"), false));
  //
  //  // add the "defaults" properties files, must be present
  //  final File nexusDefaultPropertiesFile = new File(warWebInfFile, "plexus.properties");
  //  request.getSources().add(0, new PropertiesFileEntrySource(nexusDefaultPropertiesFile, true));
  //
  //  // set basedir as LAST, no overrides for it
  //  request.getSources().add(new StaticEntrySource("bundleBasedir", basedirFile.getAbsolutePath()));
  //
  //  return Factory.create(request);
  //}
}

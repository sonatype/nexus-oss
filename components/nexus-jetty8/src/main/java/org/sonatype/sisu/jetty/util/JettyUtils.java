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

package org.sonatype.sisu.jetty.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.sonatype.appcontext.AppContext;
import org.sonatype.appcontext.AppContextRequest;
import org.sonatype.appcontext.Factory;
import org.sonatype.appcontext.source.MapEntrySource;
import org.sonatype.appcontext.source.PropertiesFileEntrySource;
import org.sonatype.appcontext.source.Sources;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.xml.XmlConfiguration;

/**
 * Utility that configures Jetty server from passed in jetty.xml file.
 *
 * @author cstamas
 */
public final class JettyUtils
{
  /**
   * System property key for "context" file location. If not present, this class will default to use
   * "jetty.properties" next to (in same directory) the Jetty XML configuration file. If property is set, and it
   * contains a relative file path, then it will be "resolved" against Jetty's XML configuration file. Otherwise, the
   * absolute path will be used (as is).
   */
  public static final String JETTY_CONTEXT_FILE_KEY = "jettyContext";

  /**
   * System property key for "direct inclusions" of other keys by AppContext.
   */
  public static final String JETTY_CONTEXT_INCLUDE_KEYS_KEY = "jettyContextIncludeKeys";

  /**
   * A system property key to turn on "appcontext dump", when the contents of appcontext is dumped using one of the
   * publishers available. Dump happens only if this property has value of "true" (Boolean.TRUE.toString()).
   */
  public static final String JETTY_CONTEXT_DUMP = "jettyContextDump";

  /**
   * A system property key to turn on "plexus compatibility", and use "plexus" alias for AppContext too, to pick up
   * env variables with "PLEXUS_" prefix and system properties with "plexus." prefix. Needed by Nexus instances for
   * example.
   */
  public static final String PLEXUS_COMPATIBILITY_KEY = "jettyPlexusCompatibility";

  private JettyUtils() {
  }

  public static AppContext configureServer(final Server server, final File jettyXml, final AppContext parent,
                                           final Map<?, ?>... overrides)
      throws IOException
  {
    final FileInputStream fis = new FileInputStream(jettyXml);
    String rawConfig;

    try {
      rawConfig = IO.toString(fis, "UTF-8");
    }
    finally {
      fis.close();
    }

    // for historical reasons, honor the "plexus" prefix too
    AppContextRequest appContextReq = null;

    if (Boolean.getBoolean(PLEXUS_COMPATIBILITY_KEY)) {
      appContextReq = Factory.getDefaultRequest("jetty", parent, Arrays.asList("plexus"));
    }
    else {
      appContextReq = Factory.getDefaultRequest("jetty", parent);
    }

    if (!Boolean.valueOf(System.getProperty(JETTY_CONTEXT_DUMP))) {
      // we do not publish anything (defaultReq does contain one "terminal" publisher)
      appContextReq.getPublishers().clear();
    }

    // fill in passed in overrides
    int ctxNo = 1;
    for (Map<?, ?> override : overrides) {
      appContextReq.getSources().add(0, new MapEntrySource("ctx" + (ctxNo++), override));
    }

    // fill in inclusions if any
    final String includedKeysString = System.getProperty(JETTY_CONTEXT_INCLUDE_KEYS_KEY);
    if (!isBlank(includedKeysString)) {
      final String[] keys = includedKeysString.split(",");
      if (keys != null && keys.length > 0) {
        appContextReq.getSources().addAll(0, Sources.getDefaultSelectTargetedSources(keys));
      }
    }

    // try jetty.properties next to XML file, if found, add it as ultimate source
    final File jettyContextFile = getContextFile(jettyXml);

    if (jettyContextFile.isFile()) {
      appContextReq.getSources().add(0, new PropertiesFileEntrySource(jettyContextFile));
    }

    final AppContext appContext = Factory.create(appContextReq);

    try {
      // Interpolate jetty.xml and apply it to server
      new XmlConfiguration(new ByteArrayInputStream(appContext.interpolate(rawConfig).getBytes("UTF-8")))
          .configure(server);
    }
    catch (Exception e) {
      final IOException ex =
          new IOException("Failed to configure Jetty server using XML configuration at: " + jettyXml);
      ex.initCause(e);
      throw ex;
    }

    return appContext;
  }

  protected static File getContextFile(final File jettyXml) {
    final String jettyContext = System.getProperty(JETTY_CONTEXT_FILE_KEY);

    if (!isBlank(jettyContext)) {
      final File jettyContextFile = new File(jettyContext);

      if (jettyContextFile.isAbsolute()) {
        return jettyContextFile;
      }
      else {
        return new File(jettyXml.getParentFile(), jettyContext);
      }
    }
    else {
      // fallback to defaults, a "jetty.properties" file next to jetty's XML configuration file
      return new File(jettyXml.getParentFile(), "jetty.properties");
    }
  }

  public static boolean isBlank(final String string) {
    return ((string == null) || (string.trim().length() == 0));
  }
}

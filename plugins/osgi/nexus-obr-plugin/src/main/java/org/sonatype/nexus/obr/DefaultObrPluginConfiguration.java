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

package org.sonatype.nexus.obr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.sisu.goodies.common.io.FileReplacer;
import org.sonatype.sisu.goodies.common.io.FileReplacer.ContentWriter;

import com.google.common.base.Throwables;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

@Component(role = ObrPluginConfiguration.class)
public class DefaultObrPluginConfiguration
    extends AbstractLoggingComponent
    implements ObrPluginConfiguration
{
  private static final String DEFAULT_OBR_PROPERTY_PATH = "/META-INF/nexus-obr-plugin/nexus-obr-plugin.properties";

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  private Map<String, String> configMap;

  private long lastLoaded;

  protected File getConfigurationFile() {
    return new File(applicationConfiguration.getConfigurationDirectory(), "nexus-obr-plugin.properties");
  }

  private synchronized Map<String, String> getConfiguration() {
    final File configFile = getConfigurationFile();
    if (null == configMap || lastLoaded < configFile.lastModified()) {
      if (!configFile.exists()) {
        writeDefaultConfiguration();
      }

      configMap = loadConfiguration(configFile);
      lastLoaded = System.currentTimeMillis();
    }

    return configMap;
  }

  private static Map<String, String> loadConfiguration(final File file) {
    final Map<String, String> newConfig = new HashMap<String, String>();

    final Properties properties = PropertyUtils.loadProperties(file);
    for (final Entry<?, ?> e : properties.entrySet()) {
      final String key = StringUtils.defaultString(e.getKey(), null);
      if (key != null) {
        newConfig.put(key, StringUtils.defaultString(e.getValue(), null));
      }
    }

    return newConfig;
  }

  private void writeDefaultConfiguration() {
    final File configurationFile = getConfigurationFile();
    getLogger().debug("Saving configuration: {}", configurationFile);
    try {
      final FileReplacer fileReplacer = new FileReplacer(configurationFile);
      // we save this file many times, don't litter backups
      fileReplacer.setDeleteBackupFile(true);
      fileReplacer.replace(new ContentWriter()
      {
        @Override
        public void write(final BufferedOutputStream output)
            throws IOException
        {
          try (final InputStream is =
                   DefaultObrPluginConfiguration.class.getResourceAsStream(DEFAULT_OBR_PROPERTY_PATH)) {
            IOUtil.copy(is, output);
          }
        }
      });
    }
    catch (IOException e) {
      getLogger().warn(
          "Could not write the OBR plugin configuration to path " + configurationFile.getAbsolutePath(), e);
      Throwables.propagate(e);
    }
  }

  private boolean getBoolean(final String key) {
    return Boolean.valueOf(getConfiguration().get(key));
  }

  public boolean isBundleCacheActive() {
    return getBoolean(ObrPluginConfiguration.CATEGORY_BUNDLE_CACHE + ".enabled");
  }
}

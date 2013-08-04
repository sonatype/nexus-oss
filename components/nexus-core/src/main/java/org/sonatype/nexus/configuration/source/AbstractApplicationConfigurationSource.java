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

package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 *
 * @author cstamas
 */
public abstract class AbstractApplicationConfigurationSource
    extends AbstractConfigurationSource
    implements ApplicationConfigurationSource
{
  /**
   * The application interpolation provider.
   */
  @Requirement
  private ApplicationInterpolatorProvider interpolatorProvider;

  /**
   * The configuration.
   */
  private Configuration configuration;

  /**
   * Flag to mark instance upgrade.
   */
  private boolean instanceUpgraded;

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  /**
   * Called by subclasses when loaded configuration is rejected for some reason.
   */
  protected void rejectConfiguration(String message, Throwable e) {
    this.configuration = null;

    if (message != null) {
      getLogger().debug(message, e);
    }
  }

  /**
   * Load configuration.
   *
   * @param file the file
   * @return the configuration
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void loadConfiguration(InputStream is)
      throws IOException, ConfigurationException
  {
    Reader fr = null;

    try {
      NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

      fr = new InputStreamReader(is);

      InterpolatorFilterReader ip = new InterpolatorFilterReader(fr, interpolatorProvider.getInterpolator());

      // read again with interpolation
      configuration = reader.read(ip);
    }
    catch (XmlPullParserException e) {
      configuration = null;

      throw new ConfigurationException("Nexus configuration file was not loaded, it has the wrong structure.", e);
    }
    finally {
      if (fr != null) {
        fr.close();
      }
    }

    // check the model version if loaded
    if (configuration != null && !Configuration.MODEL_VERSION.equals(configuration.getVersion())) {
      final String message = "Nexus configuration file was loaded but discarded, it has the wrong version number."
          + (" (expected " + Configuration.MODEL_VERSION + ", actual " + configuration.getVersion() + ")");

      rejectConfiguration(message, null);

      throw new ConfigurationException(message);
    }

    if (getConfiguration() != null) {
      getLogger().info("Configuration loaded successfully.");
    }
  }

  /**
   * Returns the default source of ConfigurationSource. May be null.
   */
  public ApplicationConfigurationSource getDefaultsSource() {
    return null;
  }

  /**
   * Is nexus instance upgraded
   */
  public boolean isInstanceUpgraded() {
    return instanceUpgraded;
  }

  /**
   * Setter for nexus instance upgraded
   */
  public void setInstanceUpgraded(boolean instanceUpgraded) {
    this.instanceUpgraded = instanceUpgraded;
  }

}

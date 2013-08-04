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

package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UnsupportedConfigurationVersionException;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Default configuration updater, using versioned Modello models. It tried to detect version signature from existing
 * file and apply apropriate modello io stuff to load configuration. It is also aware of changes across model versions.
 *
 * @author cstamas
 */
@Component(role = ApplicationConfigurationUpgrader.class)
public class DefaultApplicationConfigurationUpgrader
    extends AbstractLoggingComponent
    implements ApplicationConfigurationUpgrader
{
  @Requirement(role = SingleVersionUpgrader.class)
  private Map<String, SingleVersionUpgrader> upgraders;

  /**
   * This implementation relies to plexus registered upgraders. It will cycle through them until the configuration is
   * the needed (current) model version.
   */
  public Configuration loadOldConfiguration(File file)
      throws IOException,
             ConfigurationIsCorruptedException,
             UnsupportedConfigurationVersionException
  {
    // try to find out the model version
    String modelVersion = null;

    try {
      Reader r = new FileReader(file);

      Xpp3Dom dom = Xpp3DomBuilder.build(r);

      modelVersion = dom.getChild("version").getValue();
    }
    catch (XmlPullParserException e) {
      throw new ConfigurationIsCorruptedException(file.getAbsolutePath(), e);
    }

    if (Configuration.MODEL_VERSION.equals(modelVersion)) {
      // we have a problem here, model version is OK but we could not load it previously?
      throw new ConfigurationIsCorruptedException(file);
    }

    UpgradeMessage msg = new UpgradeMessage();

    msg.setModelVersion(modelVersion);

    // a correction for SWX
    if ("1.0".equals(modelVersion)) {
      msg.setModelVersion("1.0.0");
    }

    SingleVersionUpgrader upgrader = upgraders.get(msg.getModelVersion());

    if (upgrader != null) {
      getLogger().debug(
          "Upgrading old Nexus configuration file (version " + msg.getModelVersion() + ") from "
              + file.getAbsolutePath());

      msg.setConfiguration(upgrader.loadConfiguration(file));

      while (!Configuration.MODEL_VERSION.equals(msg.getModelVersion())) {
        if (upgrader != null) {
          upgrader.upgrade(msg);
        }
        else {
          // we could parse the XML but have no model version? Is this nexus config at all?
          throw new UnsupportedConfigurationVersionException(modelVersion, file);
        }

        upgrader = upgraders.get(msg.getModelVersion());
      }

      getLogger().info(
          "Nexus configuration file upgraded to current version " + msg.getModelVersion() + " successfully.");

      return (Configuration) msg.getConfiguration();
    }
    else {
      // we could parse the XML but have no model version? Is this nexus config at all?
      throw new UnsupportedConfigurationVersionException(modelVersion, file);
    }
  }
}

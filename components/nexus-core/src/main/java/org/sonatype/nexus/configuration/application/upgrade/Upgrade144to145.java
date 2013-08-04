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
import java.util.List;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_4_5.CProps;
import org.sonatype.nexus.configuration.model.v1_4_5.CRemoteStorage;
import org.sonatype.nexus.configuration.model.v1_4_5.upgrade.BasicVersionConverter;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Upgrades configuration model from version 1.4.4 to 1.4.5.<BR>
 * This is here for upgrade testing to a new remote storage provider. This upgrade does NOTHING unless the System
 * property: default.http.provider is set
 *
 * @author toby
 * @author velo
 */
@Component(role = SingleVersionUpgrader.class, hint = "1.4.4")
public class Upgrade144to145
    extends AbstractLoggingComponent
    implements SingleVersionUpgrader
{

  public Object loadConfiguration(File file)
      throws IOException, ConfigurationIsCorruptedException
  {
    FileReader fr = null;

    org.sonatype.nexus.configuration.model.v1_4_4.Configuration conf = null;

    try {
      // reading without interpolation to preserve user settings as variables
      fr = new FileReader(file);

      org.sonatype.nexus.configuration.model.v1_4_4.io.xpp3.NexusConfigurationXpp3Reader reader =
          new org.sonatype.nexus.configuration.model.v1_4_4.io.xpp3.NexusConfigurationXpp3Reader();

      conf = reader.read(fr);
    }
    catch (XmlPullParserException e) {
      throw new ConfigurationIsCorruptedException(file.getAbsolutePath(), e);
    }
    finally {
      if (fr != null) {
        fr.close();
      }
    }

    return conf;
  }

  public void upgrade(UpgradeMessage message)
      throws ConfigurationIsCorruptedException
  {
    org.sonatype.nexus.configuration.model.v1_4_4.Configuration oldc =
        (org.sonatype.nexus.configuration.model.v1_4_4.Configuration) message.getConfiguration();

    BasicVersionConverter versionConverter = new BasicVersionConverter()
    {
      @Override
      public CRemoteStorage convertCRemoteStorage(
          org.sonatype.nexus.configuration.model.v1_4_4.CRemoteStorage cRemoteStorage)
      {
        CRemoteStorage newRemoteStorage = super.convertCRemoteStorage(cRemoteStorage);

        // change the provider
        if (newRemoteStorage != null && System.getProperty("default.http.provider") != null) {
          newRemoteStorage.setProvider(System.getProperty("default.http.provider"));
        }

        return newRemoteStorage;
      }
    };

    org.sonatype.nexus.configuration.model.v1_4_5.Configuration newc = versionConverter.convertConfiguration(oldc);

    // NEXUS-3861
    for (org.sonatype.nexus.configuration.model.v1_4_5.CScheduledTask task : newc.getTasks()) {
      List<CProps> props = task.getProperties();
      for (CProps cProp : props) {
        if ("repositoryOrGroupId".equals(cProp.getKey())) {
          cProp.setKey("repositoryId");
          cProp.setValue(cProp.getValue().replaceFirst("repo\\_", "").replaceFirst("group\\_", ""));
        }
      }
    }

    newc.setVersion(org.sonatype.nexus.configuration.model.v1_4_5.Configuration.MODEL_VERSION);
    message.setModelVersion(org.sonatype.nexus.configuration.model.v1_4_5.Configuration.MODEL_VERSION);
    message.setConfiguration(newc);
  }

}

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

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_10_0.CErrorReporting;
import org.sonatype.nexus.configuration.model.v1_10_0.CNotification;
import org.sonatype.nexus.configuration.model.v1_10_0.CRepository;
import org.sonatype.nexus.configuration.model.v1_10_0.upgrade.BasicVersionUpgrade;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Upgrades configuration model from version 1.4.6 to 1.10.0.<BR>
 *
 * @author velo
 */
@Component(role = SingleVersionUpgrader.class, hint = "1.4.6")
public class Upgrade146to1100
    extends AbstractLoggingComponent
    implements SingleVersionUpgrader
{

  public Object loadConfiguration(File file)
      throws IOException, ConfigurationIsCorruptedException
  {
    FileReader fr = null;

    org.sonatype.nexus.configuration.model.v1_4_6.Configuration conf = null;

    try {
      // reading without interpolation to preserve user settings as variables
      fr = new FileReader(file);

      org.sonatype.nexus.configuration.model.v1_4_6.io.xpp3.NexusConfigurationXpp3Reader reader =
          new org.sonatype.nexus.configuration.model.v1_4_6.io.xpp3.NexusConfigurationXpp3Reader();

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
    org.sonatype.nexus.configuration.model.v1_4_6.Configuration oldc =
        (org.sonatype.nexus.configuration.model.v1_4_6.Configuration) message.getConfiguration();

    BasicVersionUpgrade versionConverter = new BasicVersionUpgrade()
    {
      @Override
      public CRepository upgradeCRepository(org.sonatype.nexus.configuration.model.v1_4_6.CRepository cRepository) {
        CRepository newRepo = super.upgradeCRepository(cRepository);

        Xpp3Dom dom = (Xpp3Dom) cRepository.getExternalConfiguration();
        if (cRepository.getRemoteStorage() != null && dom != null) {
          Xpp3Dom validate = dom.getChild(AbstractProxyRepositoryConfiguration.FILE_TYPE_VALIDATION);
          if (validate != null) {
            validate.setValue(Boolean.TRUE.toString());
          }
        }
        return newRepo;
      }
    };

    org.sonatype.nexus.configuration.model.v1_10_0.Configuration newc = versionConverter.upgradeConfiguration(oldc);

    // this should go into much "older" upgrader, this was a mistake!
    if (newc.getErrorReporting() == null) {
      CErrorReporting errorReporting = new CErrorReporting();
      errorReporting.setEnabled(false);
      newc.setErrorReporting(errorReporting);
    }
    if (newc.getNotification() == null) {
      CNotification notification = new CNotification();
      notification.setEnabled(false);
      newc.setNotification(notification);
    }


    newc.setVersion(org.sonatype.nexus.configuration.model.v1_10_0.Configuration.MODEL_VERSION);
    message.setModelVersion(org.sonatype.nexus.configuration.model.v1_10_0.Configuration.MODEL_VERSION);
    message.setConfiguration(newc);
  }
}

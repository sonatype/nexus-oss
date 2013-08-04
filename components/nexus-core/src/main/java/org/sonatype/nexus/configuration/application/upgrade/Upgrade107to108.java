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
import java.util.ArrayList;
import java.util.List;

import org.sonatype.configuration.upgrade.ConfigurationIsCorruptedException;
import org.sonatype.configuration.upgrade.SingleVersionUpgrader;
import org.sonatype.configuration.upgrade.UpgradeMessage;
import org.sonatype.nexus.configuration.model.v1_0_8.CProps;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepository;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.v1_0_8.CScheduledTask;
import org.sonatype.nexus.configuration.model.v1_0_8.CSecurity;
import org.sonatype.nexus.configuration.model.v1_0_8.upgrade.BasicVersionUpgrade;
import org.sonatype.nexus.logging.AbstractLoggingComponent;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role = SingleVersionUpgrader.class, hint = "1.0.7")
public class Upgrade107to108
    extends AbstractLoggingComponent
    implements SingleVersionUpgrader
{
  private BasicVersionUpgrade converter = new BasicVersionUpgrade()
  {
    @Override
    public org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup upgradeCRepositoryGroup(
        org.sonatype.nexus.configuration.model.v1_0_7.CRepositoryGroup repositoryGroup,
        org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup value)
    {
      org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryGroup group = super.upgradeCRepositoryGroup(
          repositoryGroup,
          value);
      group.setType("maven2");
      return group;
    }

    @Override
    public CSecurity upgradeCSecurity(org.sonatype.nexus.configuration.model.v1_0_7.CSecurity security,
                                      CSecurity value)
    {
      org.sonatype.nexus.configuration.model.v1_0_8.CSecurity newSecurity = super.upgradeCSecurity(security, value);
      newSecurity.removeRealm("NexusMethodAuthorizingRealm");
      newSecurity.removeRealm("NexusTargetAuthorizingRealm");
      newSecurity.removeRealm("XmlMethodAuthorizingRealm");
      newSecurity.addRealm("XmlAuthorizingRealm");

      return newSecurity;
    }
  };

  public Object loadConfiguration(File file)
      throws IOException,
             ConfigurationIsCorruptedException
  {
    FileReader fr = null;

    org.sonatype.nexus.configuration.model.v1_0_7.Configuration conf = null;

    try {
      // reading without interpolation to preserve user settings as variables
      fr = new FileReader(file);

      org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader reader = new org.sonatype.nexus.configuration.model.v1_0_7.io.xpp3.NexusConfigurationXpp3Reader();

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

  public void upgrade(UpgradeMessage message) {
    org.sonatype.nexus.configuration.model.v1_0_7.Configuration oldc = (org.sonatype.nexus.configuration.model.v1_0_7.Configuration) message
        .getConfiguration();

    org.sonatype.nexus.configuration.model.v1_0_8.Configuration newc = converter.upgradeConfiguration(oldc);

    // NEXUS-1710: enforce ID uniqueness, but also /content URL must remain unchanged for existing systems.
    // sadly, as part of upgrade, we must ensure the repoId uniqueness, which was not case in pre-1.3 nexuses
    List<String> repoIds = new ArrayList<String>();

    // repoIds are _unchanged_
    if (newc.getRepositories() != null) {
      for (CRepository repository : (List<CRepository>) newc.getRepositories()) {
        // need to check case insensitive for windows
        repoIds.add(repository.getId().toLowerCase());
      }
    }

    // shadowIds are _unchanged_ (the repo:shadow ID uniqueness was enforced in pre-1.3!)
    if (newc.getRepositoryShadows() != null) {
      for (CRepositoryShadow repository : (List<CRepositoryShadow>) newc.getRepositoryShadows()) {
        // need to check case insensitive for windows
        repoIds.add(repository.getId().toLowerCase());
      }
    }

    if (newc.getRepositoryGrouping() != null && newc.getRepositoryGrouping().getRepositoryGroups() != null) {
      for (CRepositoryGroup group : (List<CRepositoryGroup>) newc.getRepositoryGrouping().getRepositoryGroups()) {
        // need to check case insensitive for windows
        if (repoIds.contains(group.getGroupId().toLowerCase())) {
          String groupId = group.getGroupId();
          // if duped only
          group.setPathPrefix(groupId);
          group.setGroupId(groupId + "-group");

          upgradeGroupRefsInTask(newc, groupId);
        }
      }
    }

    newc.setVersion(org.sonatype.nexus.configuration.model.v1_0_8.Configuration.MODEL_VERSION);
    message.setModelVersion(org.sonatype.nexus.configuration.model.v1_0_8.Configuration.MODEL_VERSION);
    message.setConfiguration(newc);

  }

  private void upgradeGroupRefsInTask(org.sonatype.nexus.configuration.model.v1_0_8.Configuration conf,
                                      String groupId)
  {
    for (CScheduledTask task : (List<CScheduledTask>) conf.getTasks()) {
      for (CProps prop : (List<CProps>) task.getProperties()) {
        if (prop.getKey().equals("repositoryOrGroupId") && prop.getValue().equals("group_" + groupId)) {
          prop.setValue("group_" + groupId + "-group");
        }
      }
    }
  }
}

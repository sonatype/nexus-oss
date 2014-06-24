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
package com.sonatype.security.ldap.persist.upgrade;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.sonatype.security.ldap.AbstractLdapConfigurationTest;
import com.sonatype.security.ldap.persist.LdapConfigurationSource;
import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class UpgradeTest
    extends AbstractLdapConfigurationTest
{

  @Test
  public void testUpgrade() throws Exception {

    Map<String, String> results = new HashMap<String, String>();

    File upgradeDirectory = new File("target/test-classes/upgrades");

    File[] directories = upgradeDirectory.listFiles();

    for (File directory : directories) {
      // first make sure it is really a directory
      if (directory.isDirectory()) {
        String testKey = directory.getName();
        // this directory should have 2 files ldap.xml and ldap.result.xml
        File ldapXml = new File(directory, "ldap.xml");
        File ldapResultXml = new File(directory, "ldap.result.xml");

        // make sure they both exists
        if (!ldapXml.exists()) {
          results.put(testKey, "Skipping upgrade test missing 'ldap.xml'. in directory: "
              + directory.getAbsolutePath());
        }
        if (!ldapResultXml.exists()) {
          results.put(testKey, "Skipping upgrade test missing 'ldap.result.xml'. in directory: "
              + directory.getAbsolutePath());
        }

        runUpgradeTest(testKey, ldapXml, ldapResultXml);
      }
    }
  }

  private void runUpgradeTest(String testKey, File ldapXml, File ldapResultXml) throws Exception {
    //copy ldap.xml to conf dir
    File inplaceLdapXml = new File(getConfHomeDir(), "ldap.xml");
    FileUtils.copyFile(ldapXml, inplaceLdapXml);

    File testSecConfigFile = new File(ldapXml.getParentFile(), "security-configuration.xml");
    File inplaceSecConfigFile = new File(getSecurityConfiguration());
    FileUtils.copyFile(testSecConfigFile, inplaceSecConfigFile);

    LdapConfigurationSource source = this.lookup(LdapConfigurationSource.class);
    final CLdapConfiguration configuration = source.load();
    Assert.assertEquals("secret", configuration.getServers().get(0).getConnectionInfo().getSystemPassword());

    // get upgraded file as String
    String upgradeResult = FileUtils.readFileToString(inplaceLdapXml);
    String expected = FileUtils.readFileToString(ldapResultXml);
    compareConfigurations(expected, upgradeResult);
  }

}

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
package com.sonatype.nexus.testsuite.ldap.nxcm1356;

import java.util.List;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;

import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import org.junit.Assert;
import org.junit.Test;

public class NXCM1356LdapUpgradeFromOssIT
    extends AbstractLdapIT
{

  public NXCM1356LdapUpgradeFromOssIT() {
    super();
  }

  @Test
  public void testUpgrade()
      throws Exception
  {
    // Nexus should be started, lets check which Realms are loaded
    GlobalConfigurationResource globalConfig = SettingsMessageUtil.getCurrentSettings();

    // Assuming the LDAP realm
    List<String> realms = globalConfig.getSecurityRealms();
    Assert.assertEquals("Ldap realm was not configured, realms found are: " + realms, realms.get(0),
        "LdapAuthenticatingRealm");
  }

  protected boolean isStartServer() {
    return false;
  }
}

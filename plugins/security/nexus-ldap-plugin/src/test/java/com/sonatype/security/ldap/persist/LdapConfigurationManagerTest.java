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
package com.sonatype.security.ldap.persist;

import java.util.ArrayList;
import java.util.List;

import com.sonatype.security.ldap.AbstractLdapConfigurationTest;
import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.junit.Assert;
import org.junit.Test;

public class LdapConfigurationManagerTest
    extends AbstractLdapConfigurationTest
{

  @Test
  public void testGetConfig()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);
    List<CLdapServerConfiguration> ldapServers = ldapConfigManager.listLdapServerConfigurations();
    Assert.assertNotNull(ldapServers);
    Assert.assertEquals(0, ldapServers.size());
  }

  @Test
  public void testNotFoundServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);
    try {
      ldapConfigManager.getLdapServerConfiguration("INVALID_SERVER_NAME_AAAAA");
      Assert.fail("expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testAddAndGetServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);

    String id = "testAddAndGetServer";

    CLdapServerConfiguration serverConfig = new CLdapServerConfiguration();
    serverConfig.setId(id);
    serverConfig.setName(id + "-name");

    CConnectionInfo connInfo = this.buildConnectionInfo();
    serverConfig.setConnectionInfo(connInfo);
    serverConfig.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());

    ldapConfigManager.addLdapServerConfiguration(serverConfig);
    CLdapServerConfiguration result = ldapConfigManager.getLdapServerConfiguration(id);
    Assert.assertNotNull(result);

    // compare the results
    this.compareConfiguration(serverConfig, result);
  }

  @Test
  public void testUpdateServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);

    String id = "testUpdateServer";

    CLdapServerConfiguration serverConfig = new CLdapServerConfiguration();
    serverConfig.setId(id);
    serverConfig.setName(id + "-name");

    serverConfig.setConnectionInfo(this.buildConnectionInfo());
    serverConfig.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());

    // add the server
    ldapConfigManager.addLdapServerConfiguration(serverConfig);

    // NOT the same instance!
    serverConfig = new CLdapServerConfiguration();
    serverConfig.setId(id);
    serverConfig.setName(id + "-name");

    serverConfig.setConnectionInfo(this.buildConnectionInfo());
    serverConfig.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());

    serverConfig.setName(id + "newName");
    serverConfig.getConnectionInfo().setBackupMirrorHost("newbackupMirrorHost");
    serverConfig.getUserAndGroupConfig().setUserBaseDn("newuserBaseDn");
    // save the updated one
    ldapConfigManager.updateLdapServerConfiguration(serverConfig);

    // get the config
    CLdapServerConfiguration result = ldapConfigManager.getLdapServerConfiguration(id);

    // manual check a couple things
    Assert.assertEquals(id + "newName", result.getName());
    Assert.assertEquals("newbackupMirrorHost", result.getConnectionInfo().getBackupMirrorHost());
    Assert.assertEquals("newuserBaseDn", result.getUserAndGroupConfig().getUserBaseDn());

    // compare the results
    this.compareConfiguration(serverConfig, result);

    // make sure there is only the one item
    Assert.assertEquals(1, ldapConfigManager.listLdapServerConfigurations().size());

  }

  @Test
  public void testDeleteServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);

    String id = "testDeleteServer";

    CLdapServerConfiguration serverConfig = new CLdapServerConfiguration();
    serverConfig.setId(id);
    serverConfig.setName(id + "-name");

    CConnectionInfo connInfo = this.buildConnectionInfo();
    serverConfig.setConnectionInfo(connInfo);
    serverConfig.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());

    ldapConfigManager.addLdapServerConfiguration(serverConfig);
    ldapConfigManager.deleteLdapServerConfiguration(id);

    try {
      ldapConfigManager.getLdapServerConfiguration(id);
      Assert.fail("Expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testDeleteNotFoundServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);
    try {
      ldapConfigManager.deleteLdapServerConfiguration("A_MISSING_ID");
      Assert.fail("Expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testSetServerOrder()
      throws Exception
  {
    LdapConfigurationManager ldapConfigurationManager = this.lookup(LdapConfigurationManager.class);

    // add 2 ldapServers
    CLdapServerConfiguration ldapServer1 = new CLdapServerConfiguration();
    ldapServer1.setName("testSuccess1");
    ldapServer1.setConnectionInfo(this.buildConnectionInfo());
    ldapServer1.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    CLdapServerConfiguration ldapServer2 = new CLdapServerConfiguration();
    ldapServer2.setName("testSuccess2");
    ldapServer2.setConnectionInfo(this.buildConnectionInfo());
    ldapServer2.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    CLdapServerConfiguration ldapServer3 = new CLdapServerConfiguration();
    ldapServer3.setName("testSuccess3");
    ldapServer3.setConnectionInfo(this.buildConnectionInfo());
    ldapServer3.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer3);

    CLdapServerConfiguration ldapServer4 = new CLdapServerConfiguration();
    ldapServer4.setName("testSuccess4");
    ldapServer4.setConnectionInfo(this.buildConnectionInfo());
    ldapServer4.setUserAndGroupConfig(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer4);

    // the order at this point is 1, 2, 3, 4
    // we will change it to 3, 1, 4, 2
    List<String> newOrder = new ArrayList<String>();
    newOrder.add(ldapServer3.getId());
    newOrder.add(ldapServer1.getId());
    newOrder.add(ldapServer4.getId());
    newOrder.add(ldapServer2.getId());

    ldapConfigurationManager.setServerOrder(newOrder);

    // check for the same order as above
    List<CLdapServerConfiguration> ldapServers = ldapConfigurationManager.listLdapServerConfigurations();
    Assert.assertEquals(ldapServers.get(0).getId(), ldapServer3.getId());
    Assert.assertEquals(ldapServers.get(1).getId(), ldapServer1.getId());
    Assert.assertEquals(ldapServers.get(2).getId(), ldapServer4.getId());
    Assert.assertEquals(ldapServers.get(3).getId(), ldapServer2.getId());
  }

}

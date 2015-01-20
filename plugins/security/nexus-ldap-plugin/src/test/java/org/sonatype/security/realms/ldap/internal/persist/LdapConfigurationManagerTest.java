/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.realms.ldap.internal.persist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.security.realms.ldap.internal.LdapTestSupport;
import org.sonatype.security.realms.ldap.internal.persist.entity.Connection;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

public class LdapConfigurationManagerTest
    extends LdapTestSupport
{
  @Override
  protected LinkedHashMap<String, LdapConfiguration> createLdapClientConfigurations() {
    return Maps.newLinkedHashMap();
  }

  @Test
  public void testGetConfig()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);
    List<LdapConfiguration> ldapServers = ldapConfigManager.listLdapServerConfigurations();
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

    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testAddAndGetServer-name");

    Connection connInfo = this.buildConnectionInfo();
    serverConfig.setConnection(connInfo);
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    final String id = ldapConfigManager.addLdapServerConfiguration(serverConfig);
    LdapConfiguration result = ldapConfigManager.getLdapServerConfiguration(id);
    Assert.assertNotNull(result);

    // compare the results
    this.compareConfiguration(serverConfig, result);
  }

  @Test
  public void testUpdateServer()
      throws Exception
  {
    LdapConfigurationManager ldapConfigManager = this.lookup(LdapConfigurationManager.class);

    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testUpdateServer-name");

    serverConfig.setConnection(this.buildConnectionInfo());
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    // add the server
    final String id = ldapConfigManager.addLdapServerConfiguration(serverConfig);

    // NOT the same instance!
    serverConfig = new LdapConfiguration();
    serverConfig.setId(id);
    serverConfig.setName(id + "-name");

    serverConfig.setConnection(this.buildConnectionInfo());
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    serverConfig.setName(id + "newName");
    serverConfig.getConnection().setAuthScheme("newScheme");
    serverConfig.getMapping().setUserBaseDn("newuserBaseDn");
    // save the updated one
    ldapConfigManager.updateLdapServerConfiguration(serverConfig);

    // get the config
    LdapConfiguration result = ldapConfigManager.getLdapServerConfiguration(id);

    // manual check a couple things
    Assert.assertEquals(id + "newName", result.getName());
    Assert.assertEquals("newScheme", result.getConnection().getAuthScheme());
    Assert.assertEquals("newuserBaseDn", result.getMapping().getUserBaseDn());

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

    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testDeleteServer-name");

    Connection connInfo = this.buildConnectionInfo();
    serverConfig.setConnection(connInfo);
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    final String id = ldapConfigManager.addLdapServerConfiguration(serverConfig);
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
    LdapConfiguration ldapServer1 = new LdapConfiguration();
    ldapServer1.setName("testSuccess1");
    ldapServer1.setConnection(this.buildConnectionInfo());
    ldapServer1.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer1);

    LdapConfiguration ldapServer2 = new LdapConfiguration();
    ldapServer2.setName("testSuccess2");
    ldapServer2.setConnection(this.buildConnectionInfo());
    ldapServer2.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer2);

    LdapConfiguration ldapServer3 = new LdapConfiguration();
    ldapServer3.setName("testSuccess3");
    ldapServer3.setConnection(this.buildConnectionInfo());
    ldapServer3.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer3);

    LdapConfiguration ldapServer4 = new LdapConfiguration();
    ldapServer4.setName("testSuccess4");
    ldapServer4.setConnection(this.buildConnectionInfo());
    ldapServer4.setMapping(this.buildUserAndGroupAuthConfiguration());
    ldapConfigurationManager.addLdapServerConfiguration(ldapServer4);

    // the order at this point is 1, 2, 3, 4
    // we will change it to 3, 1, 4, 2
    List<String> newOrder = new ArrayList<>();
    newOrder.add(ldapServer3.getId());
    newOrder.add(ldapServer1.getId());
    newOrder.add(ldapServer4.getId());
    newOrder.add(ldapServer2.getId());

    ldapConfigurationManager.setServerOrder(newOrder);

    // check for the same order as above
    List<LdapConfiguration> ldapServers = ldapConfigurationManager.listLdapServerConfigurations();
    Assert.assertEquals(ldapServers.get(0).getId(), ldapServer3.getId());
    Assert.assertEquals(ldapServers.get(1).getId(), ldapServer1.getId());
    Assert.assertEquals(ldapServers.get(2).getId(), ldapServer4.getId());
    Assert.assertEquals(ldapServers.get(3).getId(), ldapServer2.getId());
  }

}

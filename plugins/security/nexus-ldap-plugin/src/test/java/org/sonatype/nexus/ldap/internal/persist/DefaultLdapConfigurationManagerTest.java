/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.persist;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.ldap.internal.MockLdapConfigurationSource;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.nexus.ldap.internal.persist.entity.Validator;
import org.sonatype.nexus.security.realm.RealmManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.codec.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DefaultLdapConfigurationManager}.
 */
public class DefaultLdapConfigurationManagerTest
    extends TestSupport
{
  private DefaultLdapConfigurationManager underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new DefaultLdapConfigurationManager(
        new MockLdapConfigurationSource(),
        new Validator(),
        mock(EventBus.class),
        mock(RealmManager.class)
    );
  }

  private static String encodeBase64(final String value) throws Exception {
    return Base64.encodeToString(value.getBytes(PREFERRED_ENCODING));
  }

  private Mapping buildUserAndGroupAuthConfiguration() {
    final Mapping userGroupConf = new Mapping();
    userGroupConf.setUserMemberOfAttribute("userMemberOfAttribute");
    userGroupConf.setGroupBaseDn("groupBaseDn");
    userGroupConf.setGroupIdAttribute("groupIdAttribute");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("groupMemberFormat");
    userGroupConf.setGroupObjectClass("groupObjectClass");
    userGroupConf.setLdapGroupsAsRoles(true);
    userGroupConf.setEmailAddressAttribute("emailAddressAttribute");
    userGroupConf.setUserBaseDn("userBaseDn");
    userGroupConf.setUserIdAttribute("userIdAttribute");
    userGroupConf.setUserObjectClass("userObjectClass");
    userGroupConf.setUserPasswordAttribute("userPasswordAttribute");
    userGroupConf.setUserRealNameAttribute("userRealNameAttribute");
    userGroupConf.setUserSubtree(true);
    return userGroupConf;
  }

  private Connection buildConnectionInfo() throws Exception {
    Connection connInfo = new Connection();
    connInfo.setAuthScheme("ldap");
    connInfo.setMaxIncidentsCount(3);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost(new Host(Protocol.ldap, "localhost", 386));
    connInfo.setSaslRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));
    return connInfo;
  }

  private void compareConfiguration(LdapConfiguration expected, LdapConfiguration actual) throws Exception {
    final ObjectMapper objectMapper = new ObjectMapper();
    final String expectedString = objectMapper.writeValueAsString(expected);
    final String actualString = objectMapper.writeValueAsString(actual);
    assertThat(expectedString, equalTo(actualString));
  }

  @Test
  public void testGetConfig() throws Exception {
    List<LdapConfiguration> ldapServers = underTest.listLdapServerConfigurations();
    Assert.assertNotNull(ldapServers);
    Assert.assertEquals(0, ldapServers.size());
  }

  @Test
  public void testNotFoundServer() throws Exception {
    try {
      underTest.getLdapServerConfiguration("INVALID_SERVER_NAME_AAAAA");
      Assert.fail("expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testAddAndGetServer() throws Exception {
    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testAddAndGetServer-name");

    Connection connInfo = this.buildConnectionInfo();
    serverConfig.setConnection(connInfo);
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    final String id = underTest.addLdapServerConfiguration(serverConfig);
    LdapConfiguration result = underTest.getLdapServerConfiguration(id);
    Assert.assertNotNull(result);

    // compare the results
    this.compareConfiguration(serverConfig, result);
  }

  @Test
  public void testUpdateServer() throws Exception {
    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testUpdateServer-name");

    serverConfig.setConnection(this.buildConnectionInfo());
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    // add the server
    final String id = underTest.addLdapServerConfiguration(serverConfig);

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
    underTest.updateLdapServerConfiguration(serverConfig);

    // get the config
    LdapConfiguration result = underTest.getLdapServerConfiguration(id);

    // manual check a couple things
    Assert.assertEquals(id + "newName", result.getName());
    Assert.assertEquals("newScheme", result.getConnection().getAuthScheme());
    Assert.assertEquals("newuserBaseDn", result.getMapping().getUserBaseDn());

    // compare the results
    this.compareConfiguration(serverConfig, result);

    // make sure there is only the one item
    Assert.assertEquals(1, underTest.listLdapServerConfigurations().size());
  }

  @Test
  public void testDeleteServer() throws Exception {
    LdapConfiguration serverConfig = new LdapConfiguration();
    serverConfig.setName("testDeleteServer-name");

    Connection connInfo = this.buildConnectionInfo();
    serverConfig.setConnection(connInfo);
    serverConfig.setMapping(this.buildUserAndGroupAuthConfiguration());

    final String id = underTest.addLdapServerConfiguration(serverConfig);
    underTest.deleteLdapServerConfiguration(id);

    try {
      underTest.getLdapServerConfiguration(id);
      Assert.fail("Expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testDeleteNotFoundServer() throws Exception {
    try {
      underTest.deleteLdapServerConfiguration("A_MISSING_ID");
      Assert.fail("Expected LdapServerNotFoundException");
    }
    catch (LdapServerNotFoundException e) {
      // expected
    }
  }

  @Test
  public void testSetServerOrder() throws Exception {
    // add 2 ldapServers
    LdapConfiguration ldapServer1 = new LdapConfiguration();
    ldapServer1.setName("testSuccess1");
    ldapServer1.setConnection(this.buildConnectionInfo());
    ldapServer1.setMapping(this.buildUserAndGroupAuthConfiguration());
    underTest.addLdapServerConfiguration(ldapServer1);

    LdapConfiguration ldapServer2 = new LdapConfiguration();
    ldapServer2.setName("testSuccess2");
    ldapServer2.setConnection(this.buildConnectionInfo());
    ldapServer2.setMapping(this.buildUserAndGroupAuthConfiguration());
    underTest.addLdapServerConfiguration(ldapServer2);

    LdapConfiguration ldapServer3 = new LdapConfiguration();
    ldapServer3.setName("testSuccess3");
    ldapServer3.setConnection(this.buildConnectionInfo());
    ldapServer3.setMapping(this.buildUserAndGroupAuthConfiguration());
    underTest.addLdapServerConfiguration(ldapServer3);

    LdapConfiguration ldapServer4 = new LdapConfiguration();
    ldapServer4.setName("testSuccess4");
    ldapServer4.setConnection(this.buildConnectionInfo());
    ldapServer4.setMapping(this.buildUserAndGroupAuthConfiguration());
    underTest.addLdapServerConfiguration(ldapServer4);

    // the order at this point is 1, 2, 3, 4
    // we will change it to 3, 1, 4, 2
    List<String> newOrder = new ArrayList<>();
    newOrder.add(ldapServer3.getId());
    newOrder.add(ldapServer1.getId());
    newOrder.add(ldapServer4.getId());
    newOrder.add(ldapServer2.getId());

    underTest.setServerOrder(newOrder);

    // check for the same order as above
    List<LdapConfiguration> ldapServers = underTest.listLdapServerConfigurations();
    Assert.assertEquals(ldapServers.get(0).getId(), ldapServer3.getId());
    Assert.assertEquals(ldapServers.get(1).getId(), ldapServer1.getId());
    Assert.assertEquals(ldapServers.get(2).getId(), ldapServer4.getId());
    Assert.assertEquals(ldapServers.get(3).getId(), ldapServer2.getId());
  }
}

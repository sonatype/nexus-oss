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
package org.sonatype.nexus.ldap.internal.realms;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.sonatype.nexus.ldap.internal.MockLdapConnector;
import org.sonatype.nexus.ldap.internal.connector.FailoverLdapConnector;
import org.sonatype.nexus.ldap.internal.connector.LdapConnector;
import org.sonatype.nexus.ldap.internal.connector.dao.NoLdapUserRolesFoundException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapGroupException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoSuchLdapUserException;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Host;
import org.sonatype.nexus.ldap.internal.persist.entity.Connection.Protocol;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;

import com.google.common.collect.Maps;
import org.apache.shiro.authc.AuthenticationException;
import org.junit.Assert;
import org.junit.Test;

public class FallBackConnectionTest
    extends AbstractMockLdapConnectorTest
{

  private MockLdapConnector mainConnector = null;

  private MockLdapConnector backupConnector = null;

  @Override
  protected Collection<String> getLdapServerNames() {
    return Collections.emptyList();
  }

  protected LinkedHashMap<String, LdapConfiguration> createLdapClientConfigurations() {
    final LinkedHashMap<String, LdapConfiguration> result = Maps.newLinkedHashMap();
    // create a dumb config as connectors must correspond to config (even if not used)
    final LdapConfiguration ldapConfiguration = new LdapConfiguration();
    ldapConfiguration.setId("unused"); // create will override it anyway
    ldapConfiguration.setName("unused");
    ldapConfiguration.setOrder(1);
    Connection connection = new Connection();
    connection.setSearchBase("o=sonatype");
    connection.setSystemUsername("uid=admin,ou=system");
    connection.setSystemPassword("secret");
    connection.setAuthScheme("simple");
    connection.setHost(new Host(Protocol.ldap, "localhost", 12345));
    ldapConfiguration.setConnection(connection);

    final Mapping mapping = new Mapping();
    mapping.setGroupBaseDn("ou=groups");
    mapping.setGroupIdAttribute("cn");
    mapping.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    mapping.setGroupObjectClass("organizationalRole");
    mapping.setLdapGroupsAsRoles(true);
    mapping.setEmailAddressAttribute("mail");
    mapping.setUserMemberOfAttribute("businesscategory");
    mapping.setUserBaseDn("ou=people");
    mapping.setUserIdAttribute("uid");
    mapping.setUserObjectClass("inetOrgPerson");
    mapping.setUserPasswordAttribute("userPassword");
    mapping.setUserRealNameAttribute("sn");
    mapping.setUserSubtree(true);
    ldapConfiguration.setMapping(mapping);
    // put it as "main"
    result.put("main", ldapConfiguration);
    return result;
  }

  /**
   * Here we need to reuse the ID of the one client connection we created above.
   */
  @Override
  protected void resetLdapConnectors() throws Exception {
    final List<LdapConnector> connectors = ldapManager.getLdapConnectors();
    final String id = connectors.get(0).getIdentifier();
    connectors.clear();
    this.mainConnector = this.buildMainMockServer(id);
    this.backupConnector = this.buildBackupMockServer(id);
    // important: maxIncidentCount = 1
    LdapConnector fallBackConnector = new FailoverLdapConnector(mainConnector, backupConnector, 2, 1);
    connectors.add(fallBackConnector);
  }

  @Override
  protected List<LdapConnector> getLdapConnectors() {
    // nop, unused, all done in resetLdapConnectors()
    return null;
  }

  @Test
  public void testAllUsers()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals(3, this.getLdapManager().getAllUsers().size());

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    Assert.assertEquals(2, this.getLdapManager().getAllUsers().size());

    // now start the server backup
    mainConnector.start();

    // should still be 2 users
    Assert.assertEquals(2, this.getLdapManager().getAllUsers().size());

    // now wait another 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals(3, this.getLdapManager().getAllUsers().size());
  }

  @Test
  public void testAllGroups()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals(3, this.getLdapManager().getAllGroups().size());

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    Assert.assertEquals(2, this.getLdapManager().getAllGroups().size());

    // now start the server backup
    mainConnector.start();

    // should still be 2 users
    Assert.assertEquals(2, this.getLdapManager().getAllGroups().size());

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals(3, this.getLdapManager().getAllGroups().size());
  }

  @Test
  public void testGetGroupName()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals("gamma", this.getLdapManager().getGroupName("gamma"));

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    try {
      this.getLdapManager().getGroupName("gamma");
      Assert.fail("expected NoSuchLdapGroupException");
    }
    catch (NoSuchLdapGroupException e) {
      // expected
    }

    // now start the server backup
    mainConnector.start();

    // try again, should still fail
    try {
      this.getLdapManager().getGroupName("gamma");
      Assert.fail("expected NoSuchLdapGroupException");
    }
    catch (NoSuchLdapGroupException e) {
      // expected
    }

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals("gamma", this.getLdapManager().getGroupName("gamma"));
  }

  @Test
  public void testGetUser()
      throws Exception
  {
    // all systems are good
    Assert.assertNotNull(this.getLdapManager().getUser("rwalker"));

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    try {
      this.getLdapManager().getUser("rwalker");
      Assert.fail("expected NoSuchLdapGroupException");
    }
    catch (NoSuchLdapUserException e) {
      // expected
    }

    // now start the server backup
    mainConnector.start();

    // try again, should still fail
    try {
      this.getLdapManager().getUser("rwalker");
      Assert.fail("expected NoSuchLdapGroupException");
    }
    catch (NoSuchLdapUserException e) {
      // expected
    }

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertNotNull(this.getLdapManager().getUser("rwalker"));
  }

  @Test
  public void testGetUserRoles()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals(3, this.getLdapManager().getUserRoles("rwalker").size());

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    try {
      this.getLdapManager().getUserRoles("rwalker");
      Assert.fail("expected NoLdapUserRolesFoundException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // expected
    }

    // now start the server backup
    mainConnector.start();

    // try again, should still fail
    try {
      this.getLdapManager().getUserRoles("rwalker");
      Assert.fail("expected NoLdapUserRolesFoundException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // expected
    }

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals(3, this.getLdapManager().getUserRoles("rwalker").size());
  }

  @Test
  public void testUsersWithCount()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals(3, this.getLdapManager().getUsers(3).size());

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    Assert.assertEquals(2, this.getLdapManager().getUsers(3).size());

    // now start the server backup
    mainConnector.start();

    // should still be 2 users
    Assert.assertEquals(2, this.getLdapManager().getUsers(3).size());

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals(3, this.getLdapManager().getUsers(3).size());
  }

  @Test
  public void testUserSearch()
      throws Exception
  {
    // all systems are good
    Assert.assertEquals(3, this.getLdapManager().searchUsers("", null).size());

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    Assert.assertEquals(2, this.getLdapManager().searchUsers("", null).size());

    // now start the server backup
    mainConnector.start();

    // should still be 2 users
    Assert.assertEquals(2, this.getLdapManager().searchUsers("", null).size());

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertEquals(3, this.getLdapManager().searchUsers("", null).size());
  }

  @Test
  public void testUserAuth()
      throws Exception
  {
    // all systems are good
    Assert.assertNotNull(this.getLdapManager().authenticateUser("rwalker", "rwalker123"));

    // stop the main server
    mainConnector.stop();

    // try again, this time it should hit the backup
    try {
      this.getLdapManager().authenticateUser("rwalker", "rwalker123");
      Assert.fail("expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }

    // now start the server backup
    mainConnector.start();

    // try again, should still fail
    try {
      this.getLdapManager().authenticateUser("rwalker", "rwalker123");
      Assert.fail("expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }

    // now wait another 10 seconds and we should be back at 3
    Thread.sleep(1000 * 3);

    // we should be hitting the main again
    Assert.assertNotNull(this.getLdapManager().getUser("rwalker"));
  }
}

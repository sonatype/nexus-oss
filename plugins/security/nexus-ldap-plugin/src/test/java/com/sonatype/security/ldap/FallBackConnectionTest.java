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
package com.sonatype.security.ldap;

import java.util.ArrayList;
import java.util.List;

import com.sonatype.security.ldap.connector.FailoverLdapConnector;

import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.connector.LdapConnector;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test has not been run in a while, disabled in old surefire config")
public class FallBackConnectionTest
    extends AbstractMockLdapConnectorTest
{

  private MockLdapConnector mainConnector = null;

  private MockLdapConnector backupConnector = null;

  protected List<LdapConnector> getLdapConnectors() {
    List<LdapConnector> connectors = new ArrayList<LdapConnector>();
    this.mainConnector = this.buildMainMockServer("backup");
    this.backupConnector = this.buildBackupMockServer("backup");
    LdapConnector fallBackConnector = new FailoverLdapConnector(mainConnector, backupConnector, 2);
    connectors.add(fallBackConnector);

    return connectors;
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

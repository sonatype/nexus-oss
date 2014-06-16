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

import java.util.SortedSet;
import java.util.TreeSet;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.LdapManager;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test has not been run in a while, disabled in old surefire config")
public class ConnectionBlackListTest
    extends AbstractLdapTestCase
{

  @Test
  public void testAuthenticate()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    Assert.assertNotNull(ldapManager.authenticateUser("brianf", "brianf123"));

    ldapServer.stop();

    try {
      ldapManager.authenticateUser("brianf", "brianf123");
      Assert.fail("Expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }

    ldapServer.start();

    try {
      ldapManager.authenticateUser("brianf", "brianf123");
      Assert.fail("Expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertNotNull(ldapManager.authenticateUser("brianf", "brianf123"));
  }

  @Test
  public void testGetAllGroups()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    SortedSet<String> expectedGroups = new TreeSet<String>();
    // from the default
    expectedGroups.add("public");
    expectedGroups.add("releases");
    expectedGroups.add("snapshots");

    SortedSet<String> actualGroups = ldapManager.getAllGroups();
    Assert.assertEquals(expectedGroups, actualGroups);

    ldapServer.stop();

    Assert.assertEquals(0, ldapManager.getAllGroups().size());

    ldapServer.start();

    Assert.assertEquals(0, ldapManager.getAllGroups().size());

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);

    actualGroups = ldapManager.getAllGroups();
    Assert.assertEquals(expectedGroups, actualGroups);
  }

  @Test
  public void testGetAllUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    Assert.assertEquals(3, ldapManager.getAllUsers().size());

    ldapServer.stop();
    Assert.assertEquals(0, ldapManager.getAllUsers().size());

    ldapServer.start();
    Assert.assertEquals(0, ldapManager.getAllUsers().size());

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertEquals(3, ldapManager.getAllUsers().size());
  }

  @Test
  public void testGetGroupName()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    Assert.assertEquals("releases", ldapManager.getGroupName("releases"));

    ldapServer.stop();

    try {
      ldapManager.getGroupName("releases");
      Assert.fail("Expected LdapDAOException");
    }
    catch (NoSuchLdapGroupException e) {
      // expected
    }

    ldapServer.start();

    try {
      ldapManager.getGroupName("releases");
      Assert.fail("Expected LdapDAOException");
    }
    catch (NoSuchLdapGroupException e) {
      // expected
    }

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertEquals("releases", ldapManager.getGroupName("releases"));
  }

  @Test
  public void testGetUser()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    LdapUser brianf = ldapManager.getUser("brianf");
    Assert.assertNotNull(brianf);
    Assert.assertEquals("brianf", brianf.getUsername());
    Assert.assertEquals(brianf.getUsername() + "123", brianf.getPassword());
    Assert.assertEquals("Brian Fox", brianf.getRealName());
    Assert.assertEquals(2, brianf.getMembership().size());

    ldapServer.stop();
    try {
      ldapManager.getUser("brianf");
      Assert.fail("Expected NoSuchLdapUserException");
    }
    catch (NoSuchLdapUserException e) {
      // expected
    }

    ldapServer.start();
    try {
      ldapManager.getUser("brianf");
      Assert.fail("Expected NoSuchLdapUserException");
    }
    catch (NoSuchLdapUserException e) {
      // expected
    }


    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertNotNull(ldapManager.getUser("brianf"));

  }

  @Test
  public void testGetUserRoles()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    Assert.assertEquals(2, ldapManager.getUserRoles("brianf").size());

    ldapServer.stop();
    try {
      ldapManager.getUserRoles("brianf");
      Assert.fail("Expected LdapDAOException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // expected
    }

    ldapServer.start();
    try {
      ldapManager.getUserRoles("brianf");
      Assert.fail("Expected LdapDAOException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // expected
    }

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertEquals(2, ldapManager.getUserRoles("brianf").size());
  }

  @Test
  public void testGetUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    // exact number
    Assert.assertEquals(1, ldapManager.getUsers(1).size());


    ldapServer.stop();
    Assert.assertEquals(0, ldapManager.getUsers(1).size());

    ldapServer.start();
    Assert.assertEquals(0, ldapManager.getUsers(1).size());

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertEquals(1, ldapManager.getUsers(1).size());

  }

  @Test
  public void testSearchUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);
    LdapServer ldapServer = this.getLdapServer("default");

    Assert.assertEquals(3, ldapManager.searchUsers("", null).size());

    ldapServer.stop();
    Assert.assertEquals(0, ldapManager.searchUsers("", null).size());

    ldapServer.start();
    Assert.assertEquals(0, ldapManager.searchUsers("", null).size());

    // wait 3 more sec, then we should be good
    Thread.sleep(7 * 1000);
    Assert.assertEquals(3, ldapManager.searchUsers("", null).size());
  }

  @Override
  public void tearDown()
      throws Exception
  {
    super.tearDown();
  }


}

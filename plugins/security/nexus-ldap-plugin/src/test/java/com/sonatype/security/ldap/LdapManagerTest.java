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
import java.util.SortedSet;
import java.util.TreeSet;

import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.ldap.dao.LdapUser;
import org.sonatype.security.ldap.dao.NoLdapUserRolesFoundException;
import org.sonatype.security.ldap.dao.NoSuchLdapGroupException;
import org.sonatype.security.ldap.dao.NoSuchLdapUserException;
import org.sonatype.security.ldap.realms.LdapManager;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Test has not been run in a while, disabled in old surefire config")
public class LdapManagerTest
    extends AbstractLdapTestCase
{

  @Test
  public void testAuthenticate()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    Assert.assertNotNull(ldapManager.authenticateUser("brianf", "brianf123"));

    try {
      ldapManager.authenticateUser("brianf", "junk");
      Assert.fail("Expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }

    try {
      ldapManager.authenticateUser("junk", "junk");
      Assert.fail("Expected AuthenticationException");
    }
    catch (AuthenticationException e) {
      // expected
    }
  }

  @Test
  public void testAuthenticateTest()
      throws Exception
  {
    EnterpriseLdapManager ldapManager = (EnterpriseLdapManager) this.lookup(LdapManager.class);
    LdapConfigurationManager ldapConfiguration = this.lookup(LdapConfigurationManager.class);

    CLdapServerConfiguration ldapServer = ldapConfiguration.getLdapServerConfiguration("default");
    ldapServer = this.clone(ldapServer);

    Assert.assertNotNull(ldapManager.authenticateUserTest("brianf", "brianf123", ldapServer));

    try {
      ldapManager.authenticateUserTest("brianf", "junk", ldapServer);
      Assert.fail("Expected NoSuchUserException");
    }
    catch (AuthenticationException e) {
      // expected
    }
  }

  @Test
  public void testAuthenticateTestInvalidServer()
      throws Exception
  {
    EnterpriseLdapManager ldapManager = (EnterpriseLdapManager) this.lookup(LdapManager.class);
    LdapConfigurationManager ldapConfiguration = this.lookup(LdapConfigurationManager.class);

    CLdapServerConfiguration ldapServer = ldapConfiguration.getLdapServerConfiguration("default");
    ldapServer = this.clone(ldapServer);
    ldapServer.getConnectionInfo().setHost("INVALIDSERVERNAME");

    try {
      ldapManager.authenticateUserTest("brianf", "brianf123", ldapServer);
      Assert.fail("Expected NoSuchUserException");
    }
    catch (AuthenticationException e) {
      // expected
    }
  }

  @Test
  public void testGetAllGroups()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    SortedSet<String> expectedGroups = new TreeSet<String>();
    // from the default
    expectedGroups.add("public");
    expectedGroups.add("releases");
    expectedGroups.add("snapshots");

    // from second
    expectedGroups.add("cilbup");
    expectedGroups.add("groupone");
    expectedGroups.add("grouptwo");

    // from backup
    expectedGroups.add("alpha");
    expectedGroups.add("beta");
    expectedGroups.add("gamma");

    SortedSet<String> actualGroups = ldapManager.getAllGroups();
    Assert.assertEquals(expectedGroups, actualGroups);
  }

  @Test
  public void testGetAllUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    List<String> userIds = new ArrayList<String>();
    userIds.add("brianf");
    userIds.add("cstamas");
    userIds.add("ehearn");
    userIds.add("jcoder");
    userIds.add("jgoodman");
    userIds.add("jvanzyl");
    userIds.add("mpowers");
    userIds.add("rwalker");
    userIds.add("toby");

    List<String> actualUserIds = new ArrayList<String>();
    SortedSet<LdapUser> users = ldapManager.getAllUsers();
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }

    Assert.assertEquals(userIds, actualUserIds);
  }

  @Test
  public void testGetGroupName()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    Assert.assertEquals("cilbup", ldapManager.getGroupName("cilbup"));
    Assert.assertEquals("public", ldapManager.getGroupName("public"));
    Assert.assertEquals("beta", ldapManager.getGroupName("beta"));

    try {
      ldapManager.getGroupName("INVALID_GROUP_ID");
      Assert.fail("expected NoSuchLdapGroupException");
    }
    catch (NoSuchLdapGroupException e) {
      // expected
    }
  }

  @Test
  public void testGetUser()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    LdapUser toby = ldapManager.getUser("toby");
    Assert.assertNotNull(toby);
    Assert.assertEquals("toby", toby.getUsername());
    Assert.assertEquals(toby.getUsername() + "123", toby.getPassword());
    Assert.assertEquals("Toby Stevens", toby.getRealName());
    Assert.assertEquals(2, toby.getMembership().size());

    LdapUser cstamas = ldapManager.getUser("cstamas");
    Assert.assertNotNull(cstamas);
    Assert.assertEquals("cstamas", cstamas.getUsername());
    Assert.assertEquals(cstamas.getUsername() + "123", cstamas.getPassword());
    Assert.assertEquals("Tamas Cservenak", cstamas.getRealName());
    Assert.assertEquals(2, cstamas.getMembership().size());

    LdapUser jgoodman = ldapManager.getUser("jgoodman");
    Assert.assertNotNull(jgoodman);
    Assert.assertEquals("jgoodman", jgoodman.getUsername());
    Assert.assertEquals(jgoodman.getUsername() + "123", jgoodman.getPassword());
    Assert.assertEquals("Joseph M. Goodman", jgoodman.getRealName());
    Assert.assertEquals(2, jgoodman.getMembership().size());

    try {
      ldapManager.getUser("INVALID_USER_ID");
      Assert.fail("expected NoSuchLdapUserException");
    }
    catch (NoSuchLdapUserException e) {
      // expected
    }
  }

  @Test
  public void testGetUserRoles()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    Assert.assertEquals(2, ldapManager.getUserRoles("toby").size());
    Assert.assertEquals(2, ldapManager.getUserRoles("cstamas").size());
    Assert.assertEquals(2, ldapManager.getUserRoles("jgoodman").size());

    try {
      ldapManager.getUserRoles("INVALID_USER_ID");
      Assert.fail("expected NoLdapUserRolesFoundException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // expected
    }
  }

  @Test
  public void testGetUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    List<String> userIds = new ArrayList<String>();
    userIds.add("brianf"); // 1
    userIds.add("cstamas"); // 1
    userIds.add("ehearn"); // 3
    userIds.add("jcoder");// 2
    userIds.add("jgoodman");// 3
    userIds.add("jvanzyl"); // 1
    userIds.add("mpowers");// 2
    userIds.add("rwalker");// 3
    userIds.add("toby");// 2

    // exact number

    List<String> actualUserIds = new ArrayList<String>();
    SortedSet<LdapUser> users = ldapManager.getUsers(userIds.size());
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }

    Assert.assertEquals(userIds, actualUserIds);

    // all + 1
    actualUserIds.clear();
    users = ldapManager.getUsers(userIds.size() + 1);
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }
    Assert.assertEquals(userIds, actualUserIds);

    // all -1
    userIds = new ArrayList<String>();
    actualUserIds.clear();
    userIds.add("brianf");
    userIds.add("cstamas");
    userIds.add("jvanzyl");
    userIds.add("jcoder");// 2
    userIds.add("mpowers");// 2
    userIds.add("toby");// 2

    actualUserIds.clear();
    users = ldapManager.getUsers(8);
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }
    // we should have all of the first server and then part of the second
    Assert.assertTrue("actualUserIds: " + actualUserIds + " does not contain all of: " + userIds, actualUserIds
        .containsAll(userIds));
    Assert.assertEquals(8, actualUserIds.size());

    // 3 but just the first server
    userIds = new ArrayList<String>();
    actualUserIds.clear();
    userIds.add("brianf");
    userIds.add("cstamas");
    userIds.add("jvanzyl");

    actualUserIds.clear();
    users = ldapManager.getUsers(3);
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }
    Assert.assertEquals(userIds, actualUserIds);

  }

  @Test
  public void testSearchUsers()
      throws Exception
  {
    LdapManager ldapManager = this.lookup(LdapManager.class);

    List<String> userIds = new ArrayList<String>();
    userIds.add("jcoder");
    userIds.add("jgoodman");
    userIds.add("jvanzyl");

    // exact number

    List<String> actualUserIds = new ArrayList<String>();
    SortedSet<LdapUser> users = ldapManager.searchUsers("j", null);
    for (LdapUser ldapUser : users) {
      actualUserIds.add(ldapUser.getUsername());
    }
    Assert.assertEquals(userIds, actualUserIds);

    Assert.assertEquals(9, ldapManager.searchUsers("", null).size());
    Assert.assertEquals(0, ldapManager.searchUsers("z", null).size());
  }

  private CLdapServerConfiguration clone(CLdapServerConfiguration ldapServer) {
    XStream xstream = new XStream();
    return (CLdapServerConfiguration) xstream.fromXML(xstream.toXML(ldapServer));
  }
}

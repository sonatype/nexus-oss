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
package org.sonatype.nexus.ldap.internal.connector.dao;

import java.util.Set;

import javax.naming.ldap.InitialLdapContext;

import org.sonatype.nexus.ldap.internal.LdapITSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DynamicGroupsIT
    extends LdapITSupport
{
  @Test
  public void testUserManagerWithDynamicGroups()
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setUserRealNameAttribute("cn");
    configuration.setUserMemberOfAttribute("businesscategory");
    configuration.setLdapGroupsAsRoles(true);

    LdapUserDAO lum = lookup(LdapUserDAO.class);

    LdapUser user = lum.getUser("cstamas", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("cstamas"));
    assertThat(user.getPassword(), equalTo("cstamas123"));
    assertThat(user.getMembership(), hasSize(2));
    assertTrue(user.getMembership().contains("public"));
    assertTrue(user.getMembership().contains("snapshots"));

    user = lum.getUser("brianf", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("brianf"));
    assertThat(user.getPassword(), equalTo("brianf123"));
    assertThat(user.getMembership(), hasSize(2));
    assertTrue(user.getMembership().contains("public"));
    assertTrue(user.getMembership().contains("releases"));

    user = lum.getUser("jvanzyl", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("jvanzyl"));
    assertThat(user.getPassword(), equalTo("jvanzyl123"));
    assertThat(user.getMembership(), hasSize(3));
    assertTrue(user.getMembership().contains("public"));
    assertTrue(user.getMembership().contains("releases"));
    assertTrue(user.getMembership().contains("snapshots"));

    try {
      lum.getUser("intruder", initialContext, configuration);
      fail();
    }
    catch (NoSuchLdapUserException e) {
      // good
    }
  }

  @Test
  public void testUserManagerWithDynamicGroupsDisabled()
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setUserRealNameAttribute("cn");
    configuration.setUserMemberOfAttribute("businesscategory");
    configuration.setLdapGroupsAsRoles(false);

    LdapUserDAO lum = lookup(LdapUserDAO.class);

    LdapUser user = lum.getUser("cstamas", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("cstamas"));
    assertThat(user.getPassword(), equalTo("cstamas123"));
    assertThat(user.getMembership(), hasSize(0));

    user = lum.getUser("brianf", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("brianf"));
    assertThat(user.getPassword(), equalTo("brianf123"));
    assertThat(user.getMembership(), hasSize(0));

    user = lum.getUser("jvanzyl", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("jvanzyl"));
    assertThat(user.getPassword(), equalTo("jvanzyl123"));
    assertThat(user.getMembership(), hasSize(0));

    try {
      lum.getUser("intruder", initialContext, configuration);
      fail();
    }
    catch (NoSuchLdapUserException e) {
      // good
    }
  }

  @Test
  public void testGroupManagerWithDynamicGroups()
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setUserRealNameAttribute("cn");
    configuration.setUserMemberOfAttribute("businesscategory");
    configuration.setLdapGroupsAsRoles(true);

    LdapGroupDAO lgm = lookup(LdapGroupDAO.class);

    Set<String> groups = lgm.getGroupMembership("cstamas", initialContext, configuration);

    assertThat(groups, hasSize(2));
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("snapshots"));

    groups = lgm.getGroupMembership("brianf", initialContext, configuration);
    assertThat(groups, hasSize(2));
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("releases"));

    groups = lgm.getGroupMembership("jvanzyl", initialContext, configuration);
    assertThat(groups, hasSize(3));
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("releases"));
    assertTrue(groups.contains("snapshots"));

    try {
      lgm.getGroupMembership("intruder", initialContext, configuration);
      fail();
    }
    catch (NoLdapUserRolesFoundException e) {
      // good
    }
  }

  @Test
  public void testGroupManagerWithDynamicGroupsDisabled()
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setUserRealNameAttribute("cn");
    configuration.setUserMemberOfAttribute("businesscategory");
    configuration.setLdapGroupsAsRoles(false);

    LdapGroupDAO lgm = lookup(LdapGroupDAO.class);

    try {
      lgm.getGroupMembership("cstamas", initialContext, configuration);
      fail("Expected NoLdapUserRolesFoundException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // good
    }

    try {
      lgm.getGroupMembership("intruder", initialContext, configuration);
      fail("Expected NoLdapUserRolesFoundException");
    }
    catch (NoLdapUserRolesFoundException e) {
      // good
    }
  }

}

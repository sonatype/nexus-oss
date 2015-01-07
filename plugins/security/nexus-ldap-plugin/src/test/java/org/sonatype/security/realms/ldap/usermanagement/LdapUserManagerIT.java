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
package org.sonatype.security.realms.ldap.usermanagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authorization.Role;
import org.sonatype.security.configuration.model.SecurityConfiguration;
import org.sonatype.security.model.Configuration;
import org.sonatype.security.realms.ldap.LdapPlugin;
import org.sonatype.security.realms.ldap.internal.LdapITSupport;
import org.sonatype.security.realms.ldap.internal.SecurityTestSupportSecurity;
import org.sonatype.security.realms.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.security.realms.ldap.internal.persist.entity.Mapping;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserManager;
import org.sonatype.security.usermanagement.UserSearchCriteria;

import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;

public class LdapUserManagerIT
    extends LdapITSupport
{
  /**
   * This IT uses real security manager, so it needs it in container.
   */
  @Override
  protected SecuritySystem getBoundSecuritySystem() {
    return null;
  }

  @Override
  protected SecurityConfiguration getSecurityConfig() {
    return SecurityTestSupportSecurity.securityWithLdapRealm();
  }

  @Override
  protected Configuration getSecurityModelConfig() {
    return LdapUserManagerITSecurity.securityModel();
  }

  private SecuritySystem getSecuritySystem()
      throws Exception
  {
    return this.lookup(SecuritySystem.class);
  }

  private UserManager getUserManager()
      throws Exception
  {
    return this.lookup(UserManager.class, LdapPlugin.USER_SOURCE);
  }

  @Override
  protected LdapConfiguration createLdapClientConfigurationForServer(final String name, final int order,
                                                                     final LdapServer ldapServer)
  {
    final LdapConfiguration ldapConfiguration = super.createLdapClientConfigurationForServer(name, order, ldapServer);

    // adjust it, ITs by default uses different groups
    final Mapping mapping = ldapConfiguration.getMapping();
    mapping.setGroupMemberFormat("uid=${username},ou=people,o=sonatype");
    mapping.setGroupObjectClass("groupOfUniqueNames");
    mapping.setGroupBaseDn("ou=groups");
    mapping.setGroupIdAttribute("cn");
    mapping.setGroupMemberAttribute("uniqueMember");
    mapping.setUserObjectClass("inetOrgPerson");
    mapping.setUserBaseDn("ou=people");
    mapping.setUserIdAttribute("uid");
    mapping.setUserPasswordAttribute("userPassword");
    mapping.setUserRealNameAttribute("sn");
    mapping.setEmailAddressAttribute("mail");
    mapping.setUserSubtree(false);
    mapping.setLdapGroupsAsRoles(true);

    return ldapConfiguration;
  }

  @Test
  public void testGetUserFromUserManager()
      throws Exception
  {

    SecuritySystem securitySystem = this.getSecuritySystem();
    securitySystem.start();
    User user = securitySystem.getUser("cstamas");
    Assert.assertNotNull(user);
    Assert.assertEquals("cstamas", user.getUserId());
    Assert.assertEquals("cstamas@sonatype.com", user.getEmailAddress());
    Assert.assertEquals("Tamas Cservenak", user.getName());

    Set<String> roleIds = this.getUserRoleIds(user);
    assertThat(roleIds, containsInAnyOrder("repoconsumer", "anonymous", "developer"));
  }

  @Test
  public void testGetUserFromLocator()
      throws Exception
  {
    UserManager userLocator = this.getUserManager();
    User user = userLocator.getUser("cstamas");
    assertNotNull(user);
    Assert.assertEquals("cstamas", user.getUserId());
    Assert.assertEquals("cstamas@sonatype.com", user.getEmailAddress());
    Assert.assertEquals("Tamas Cservenak", user.getName());
  }

  @Test
  public void testGetUserIds()
      throws Exception
  {
    UserManager userLocator = this.getUserManager();
    Set<String> userIds = userLocator.listUserIds();
    assertTrue(userIds.contains("cstamas"));
    assertTrue(userIds.contains("brianf"));
    assertTrue(userIds.contains("jvanzyl"));
    assertTrue(userIds.contains("jdcasey"));
    Assert.assertEquals("Ids: " + userIds, 4, userIds.size());
  }

  @Test
  public void testSearch()
      throws Exception
  {
    UserManager userLocator = this.getUserManager();
    Set<User> users = userLocator.searchUsers(new UserSearchCriteria("j"));

    assertNotNull(this.getById(users, "jvanzyl"));
    assertNotNull(this.getById(users, "jdcasey"));
    Assert.assertEquals("Users: " + users, 2, users.size());
  }

  @Test
  public void testEffectiveSearch()
      throws Exception
  {
    UserManager userLocator = this.getUserManager();

    Set<String> allRoleIds = new HashSet<String>();
    for (Role role : this.getSecuritySystem().listRoles()) {
      allRoleIds.add(role.getRoleId());
    }

    UserSearchCriteria criteria = new UserSearchCriteria("j", allRoleIds, null);

    Set<User> users = userLocator.searchUsers(criteria);

    assertNotNull(this.getById(users, "jvanzyl"));
    Assert.assertEquals("Users: " + users, 1, users.size());
  }

  @Test
  public void testGetUsers()
      throws Exception
  {
    UserManager userLocator = this.getUserManager();
    Set<User> users = userLocator.listUsers();

    User cstamas = this.getById(users, "cstamas");
    Assert.assertEquals("cstamas", cstamas.getUserId());
    Assert.assertEquals("cstamas@sonatype.com", cstamas.getEmailAddress());
    Assert.assertEquals("Tamas Cservenak", cstamas.getName());

    User brianf = this.getById(users, "brianf");
    Assert.assertEquals("brianf", brianf.getUserId());
    Assert.assertEquals("brianf@sonatype.com", brianf.getEmailAddress());
    Assert.assertEquals("Brian Fox", brianf.getName());

    User jvanzyl = this.getById(users, "jvanzyl");
    Assert.assertEquals("jvanzyl", jvanzyl.getUserId());
    Assert.assertEquals("jvanzyl@sonatype.com", jvanzyl.getEmailAddress());
    Assert.assertEquals("Jason Van Zyl", jvanzyl.getName());

    User jdcasey = this.getById(users, "jdcasey");
    Assert.assertEquals("jdcasey", jdcasey.getUserId());
    Assert.assertEquals("jdcasey@sonatype.com", jdcasey.getEmailAddress());
    Assert.assertEquals("John Casey", jdcasey.getName());

    Assert.assertEquals("Ids: " + users, 4, users.size());
  }

  private User getById(Set<User> users, String userId) {
    for (User User : users) {
      if (User.getUserId().equals(userId)) {
        return User;
      }
    }
    fail("Failed to find user: " + userId + " in list.");
    return null;
  }

  private Set<String> getUserRoleIds(User user) {
    Set<String> roleIds = new HashSet<String>();
    for (RoleIdentifier role : user.getRoles()) {
      roleIds.add(role.getRoleId());
    }
    return roleIds;
  }

  @Test
  public void testOrderOfUserSearch()
      throws Exception
  {
    SecuritySystem securitySystem = this.getSecuritySystem();
    securitySystem.start();

    List<String> realms = new ArrayList<String>();
    realms.add("NexusAuthenticatingRealm");
    realms.add(LdapPlugin.REALM_NAME);

    securitySystem.setRealms(realms);

    // the user developer is in both realms, we need to make sure the order is honored
    User user = securitySystem.getUser("brianf");
    Assert.assertEquals("default", user.getSource());

    realms.clear();
    realms.add(LdapPlugin.REALM_NAME);
    realms.add("NexusAuthenticatingRealm");
    securitySystem.setRealms(realms);

    // now the user should belong to the LDAP realm

    user = securitySystem.getUser("brianf");
    Assert.assertEquals("LDAP", user.getSource());

  }
}

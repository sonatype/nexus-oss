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
package org.sonatype.nexus.ldap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonatype.nexus.ldap.internal.LdapConstants;
import org.sonatype.nexus.ldap.internal.LdapITSupport;
import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.config.MemorySecurityConfiguration;
import org.sonatype.nexus.security.internal.AuthenticatingRealmImpl;
import org.sonatype.nexus.security.realm.MemoryRealmConfigurationStore;
import org.sonatype.nexus.security.realm.RealmConfiguration;
import org.sonatype.nexus.security.realm.RealmConfigurationStore;
import org.sonatype.nexus.security.realm.RealmManager;
import org.sonatype.nexus.security.role.Role;
import org.sonatype.nexus.security.role.RoleIdentifier;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserSearchCriteria;
import org.sonatype.sisu.litmus.testsupport.ldap.LdapServer;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

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
  protected MemorySecurityConfiguration getSecurityModelConfig() {
    return LdapUserManagerITSecurity.securityModel();
  }

  private SecuritySystem getSecuritySystem() throws Exception {
    return this.lookup(SecuritySystem.class);
  }

  private UserManager getUserManager() throws Exception {
    return this.lookup(UserManager.class, LdapConstants.USER_SOURCE);
  }

  @Override
  protected void customizeModules(final List<Module> modules) {
    modules.add(new Module()
    {
      @Override
      public void configure(final Binder binder) {
        binder.bind(RealmConfigurationStore.class)
            .to(MemoryRealmConfigurationStore.class);
      }
    });
    super.customizeModules(modules);
  }

  @Override
  protected LdapConfiguration createLdapClientConfigurationForServer(final String name,
                                                                     final int order,
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
  public void testGetUserFromUserManager() throws Exception {
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
  public void testGetUserFromLocator() throws Exception {
    UserManager userLocator = this.getUserManager();
    User user = userLocator.getUser("cstamas");
    assertNotNull(user);
    Assert.assertEquals("cstamas", user.getUserId());
    Assert.assertEquals("cstamas@sonatype.com", user.getEmailAddress());
    Assert.assertEquals("Tamas Cservenak", user.getName());
  }

  @Test
  public void testGetUserIds() throws Exception {
    UserManager userLocator = this.getUserManager();
    Set<String> userIds = userLocator.listUserIds();
    assertTrue(userIds.contains("cstamas"));
    assertTrue(userIds.contains("brianf"));
    assertTrue(userIds.contains("jvanzyl"));
    assertTrue(userIds.contains("jdcasey"));
    Assert.assertEquals("Ids: " + userIds, 4, userIds.size());
  }

  @Test
  public void testSearch() throws Exception {
    UserManager userLocator = this.getUserManager();
    Set<User> users = userLocator.searchUsers(new UserSearchCriteria("j"));

    assertNotNull(this.getById(users, "jvanzyl"));
    assertNotNull(this.getById(users, "jdcasey"));
    Assert.assertEquals("Users: " + users, 2, users.size());
  }

  @Test
  public void testEffectiveSearch() throws Exception {
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
  public void testGetUsers() throws Exception {
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
  public void testOrderOfUserSearch() throws Exception {
    SecuritySystem securitySystem = this.getSecuritySystem();
    securitySystem.start();

    RealmManager realmManager = lookup(RealmManager.class);
    RealmConfiguration realmConfiguration;

    realmConfiguration = new RealmConfiguration();
    realmConfiguration.setRealmNames(ImmutableList.of(AuthenticatingRealmImpl.NAME, LdapConstants.REALM_NAME));
    realmManager.setConfiguration(realmConfiguration);

    // the user developer is in both realms, we need to make sure the order is honored
    User user = securitySystem.getUser("brianf");
    Assert.assertEquals("default", user.getSource());

    // change realm order
    realmConfiguration = new RealmConfiguration();
    realmConfiguration.setRealmNames(ImmutableList.of(LdapConstants.REALM_NAME, AuthenticatingRealmImpl.NAME));
    realmManager.setConfiguration(realmConfiguration);

    // now the user should belong to the LDAP realm

    user = securitySystem.getUser("brianf");
    Assert.assertEquals("LDAP", user.getSource());
  }
}

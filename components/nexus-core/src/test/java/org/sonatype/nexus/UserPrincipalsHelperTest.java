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
package org.sonatype.nexus;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.sonatype.nexus.security.SecuritySystem;
import org.sonatype.nexus.security.UserPrincipalsHelper;
import org.sonatype.nexus.security.config.PreconfiguredSecurityConfigurationSource;
import org.sonatype.nexus.security.config.SecurityConfigurationSource;
import org.sonatype.nexus.security.settings.PreconfiguredSecuritySettingsSource;
import org.sonatype.nexus.security.settings.SecuritySettingsSource;
import org.sonatype.nexus.security.user.AbstractReadOnlyUserManager;
import org.sonatype.nexus.security.user.NoSuchUserManagerException;
import org.sonatype.nexus.security.user.User;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserSearchCriteria;
import org.sonatype.nexus.security.user.UserStatus;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;

// FIXME: This lives in nexus-core due to heavy reliance on legacy UT classes

/**
 * Tests for {@link UserPrincipalsHelper}.
 */
public class UserPrincipalsHelperTest
    extends NexusAppTestSupport
{
  private SecuritySystem securitySystem;

  private UserPrincipalsHelper underTest;

  @Override
  protected boolean runWithSecurityDisabled() {
    return false;
  }

  @Override
  protected void customizeModules(final List<Module> modules) {
    super.customizeModules(modules);
    modules.add(new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(Realm.class).annotatedWith(Names.named("TestPrincipalsRealm")).to(TestRealm.class);
        bind(UserManager.class).annotatedWith(Names.named("TestPrincipalsUserManager")).to(TestUserManager.class);
        bind(SecuritySettingsSource.class)
            .annotatedWith(Names.named("default"))
            .toInstance(new PreconfiguredSecuritySettingsSource(NexusAppTestSupportSecurity.security()));
        bind(SecurityConfigurationSource.class)
            .annotatedWith(Names.named("default"))
            .toInstance(new PreconfiguredSecurityConfigurationSource(NexusAppTestSupportSecurity.securityModel()));
      }
    });
  }
  
  @Override
  protected void setUp() throws Exception {
    TestUserManager.status = UserStatus.active;
    TestUserManager.userDeleted = false;

    super.setUp();

    securitySystem = lookup(SecuritySystem.class);
    securitySystem.start();
    
    underTest = lookup(UserPrincipalsHelper.class);
  }

  @Override
  public void tearDown() throws Exception {
    try {
      ThreadContext.remove();
      lookup(CacheManager.class).shutdown();
    }
    finally {
      super.tearDown();
    }
  }

  @Test
  public void testFindUserManagerHandlesNull() throws Exception {
    try {
      underTest.findUserManager(null);

      assertThat("Expected NoSuchUserManagerException", false);
    }
    catch (final NoSuchUserManagerException e) {
      // expected...
    }
  }

  @Test
  public void testNoSuchUserManager() throws Exception {
    try {
      underTest.findUserManager(new SimplePrincipalCollection("badUser", "badRealm"));

      assertThat("Expected NoSuchUserManagerException", false);
    }
    catch (final NoSuchUserManagerException e) {
      // expected...
    }
  }

  @Test
  public void testFindUserManager() throws Exception {
    final Subject subject = login("deployment", "deployment123");
    try {
      final PrincipalCollection principals = subject.getPrincipals();
      final UserManager userManager = underTest.findUserManager(principals);

      assertThat(principals.getPrimaryPrincipal().toString(), isIn(userManager.listUserIds()));
      assertThat(userManager.getAuthenticationRealmName(), isIn(principals.getRealmNames()));
    }
    finally {
      subject.logout();
    }
  }

  @Test
  public void testFindUserManagerNonDefaultRealm() throws Exception {
    final List<String> realms = securitySystem.getRealms();
    realms.add("TestPrincipalsRealm");
    securitySystem.setRealms(realms);

    final Subject subject = login("tempUser", "tempPass");
    try {
      final PrincipalCollection principals = subject.getPrincipals();
      final UserManager userManager = underTest.findUserManager(principals);

      assertThat(principals.getPrimaryPrincipal().toString(), isIn(userManager.listUserIds()));
      assertThat(userManager.getAuthenticationRealmName(), isIn(principals.getRealmNames()));
    }
    finally {
      subject.logout();
    }
  }

  @Test
  public void testGetUserStatusHandlesNull() throws Exception {
    try {
      underTest.getUserStatus(null);

      assertThat("Expected UserNotFoundException", false);
    }
    catch (final UserNotFoundException e) {
      // expected...
    }
  }

  @Test
  public void testUnknownUser() throws Exception {
    try {
      underTest.getUserStatus(new SimplePrincipalCollection("badUser", "badRealm"));

      assertThat("Expected UserNotFoundException", false);
    }
    catch (final UserNotFoundException e) {
      // expected...
    }
  }

  @Test
  public void testGetUserStatus() throws Exception {
    final Subject subject = login("deployment", "deployment123");
    try {
      final PrincipalCollection principals = subject.getPrincipals();

      assertThat(underTest.getUserStatus(principals), is(UserStatus.active));
    }
    finally {
      subject.logout();
    }
  }

  @Test
  public void testGetUserStatusNonDefaultRealm() throws Exception {
    final List<String> realms = securitySystem.getRealms();
    realms.add("TestPrincipalsRealm");
    securitySystem.setRealms(realms);

    final Subject subject = login("tempUser", "tempPass");
    try {
      final PrincipalCollection principals = subject.getPrincipals();

      // check status is passed through
      assertThat(underTest.getUserStatus(principals), is(UserStatus.active));
      TestUserManager.status = UserStatus.disabled;
      assertThat(underTest.getUserStatus(principals), is(UserStatus.disabled));
      TestUserManager.status = UserStatus.locked;
      assertThat(underTest.getUserStatus(principals), is(UserStatus.locked));
      TestUserManager.status = UserStatus.active;
      assertThat(underTest.getUserStatus(principals), is(UserStatus.active));

      TestUserManager.userDeleted = true;

      try {
        underTest.getUserStatus(principals);

        assertThat("Expected UserNotFoundException", false);
      }
      catch (final UserNotFoundException e) {
        // expected...
      }
    }
    finally {
      subject.logout();
    }
  }

  private Subject login(String username, String password) throws AuthenticationException {
    return securitySystem.login(new UsernamePasswordToken(username, password));
  }

  @Singleton
  static final class TestUserManager
      extends AbstractReadOnlyUserManager
  {
    static UserStatus status = UserStatus.active;

    static boolean userDeleted;

    public String getSource() {
      return "TestPrincipalsUserManager";
    }

    public String getAuthenticationRealmName() {
      return "TestPrincipalsRealm";
    }

    @Override
    public Set<User> listUsers() {
      if (!userDeleted) {
        final User user = new User();
        user.setUserId("tempUser");
        user.setStatus(status);
        return Collections.singleton(user);
      }
      return Collections.emptySet();
    }

    public User getUser(String userId) {
      for (final User user : listUsers()) {
        if (user.getUserId().equals(userId)) {
          return user;
        }
      }
      return null;
    }

    public Set<String> listUserIds() {
      final Set<String> userIds = new HashSet<String>();
      for (final User user : listUsers()) {
        userIds.add(user.getUserId());
      }
      return userIds;
    }

    public Set<User> searchUsers(UserSearchCriteria criteria) {
      return filterListInMemeory(listUsers(), criteria);
    }
  }

  @Singleton
  static final class TestRealm
      extends AuthorizingRealm
  {
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
      if ("tempUser".equals(((UsernamePasswordToken) token).getUsername())) {
        return new SimpleAuthenticationInfo("tempUser", "tempPass", "TestPrincipalsRealm");
      }
      return null;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
      return null;
    }
  }
}

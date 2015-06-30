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
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.NamingException;

import org.sonatype.nexus.ldap.internal.LdapConstants;
import org.sonatype.nexus.ldap.internal.connector.dao.LdapDAOException;
import org.sonatype.nexus.ldap.internal.connector.dao.NoLdapUserRolesFoundException;
import org.sonatype.nexus.ldap.internal.events.LdapClearCacheEvent;
import org.sonatype.nexus.ldap.internal.realms.LdapManager;
import org.sonatype.sisu.goodies.common.Loggers;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.base.Strings;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.realm.ldap.AbstractLdapRealm;
import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.apache.shiro.subject.PrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

@Named(LdapConstants.REALM_NAME)
@Singleton
@Description("LDAP Realm")
public class LdapRealm
    extends AbstractLdapRealm
{
  private final Logger logger = Loggers.getLogger(getClass());

  private final LdapManager ldapManager;

  @Inject
  public LdapRealm(final EventBus eventBus, final LdapManager ldapManager) {
    this.ldapManager = checkNotNull(ldapManager);
    setName(LdapConstants.REALM_NAME);
    setAuthenticationCachingEnabled(true);
    setAuthorizationCachingEnabled(true);
    // using simple credentials matcher
    setCredentialsMatcher(new SimpleCredentialsMatcher());

    eventBus.register(this);
  }

  /**
   * Handler that clears caches when needed.
   *
   * @since 2.8
   */
  @AllowConcurrentEvents
  @Subscribe
  public void on(final LdapClearCacheEvent evt) {
    clearIfNonNull(getAuthenticationCache());
    clearIfNonNull(getAuthorizationCache());
  }

  /**
   * Clears Shiro cache if passed instance is not {@code null}.
   *
   * @since 2.8
   */
  protected void clearIfNonNull(@Nullable final Cache cache) {
    if (cache != null) {
      cache.clear();
    }
  }

  @Override
  protected AuthenticationInfo queryForAuthenticationInfo(AuthenticationToken token,
                                                          LdapContextFactory ldapContextFactory)
      throws NamingException
  {
    UsernamePasswordToken upToken = (UsernamePasswordToken) token;
    String username = upToken.getUsername();
    String pass = String.valueOf(upToken.getPassword());

    // Verify non-empty password
    if (Strings.isNullOrEmpty(pass)) {
      throw new AuthenticationException("Password must not be empty");
    }

    this.ldapManager.authenticateUser(username, pass);

    // creating AuthInfo with plain pass (relates to creds matcher too)
    return new SimpleAuthenticationInfo(username, pass.toCharArray(), getName());
  }

  @Override
  protected AuthorizationInfo queryForAuthorizationInfo(PrincipalCollection principals,
                                                        LdapContextFactory ldapContextFactory)
      throws NamingException
  {
    // only authorize users from this realm
    if (principals.getRealmNames().contains(this.getName())) {

      Set<String> roles = new HashSet<String>();
      String username = principals.getPrimaryPrincipal().toString();
      try {
        roles = this.ldapManager.getUserRoles(username);
      }
      catch (LdapDAOException e) {
        this.logger.error(e.getMessage(), e);
        throw new NamingException(e.getMessage());
      }
      catch (NoLdapUserRolesFoundException e) {
        this.logger.debug("User: " + username + " does not have any ldap roles.", e);
      }

      return new SimpleAuthorizationInfo(roles);
    }
    return null;
  }
}

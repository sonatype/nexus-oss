/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.rutauth.internal;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.security.SecuritySystem;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserSearchCriteria;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.eclipse.sisu.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Rut Auth Realm.
 *
 * @since 2.7
 */
@Named(RutAuthRealm.ID)
@Singleton
@Description(RutAuthRealm.DESCRIPTION)
public class RutAuthRealm
    extends AuthenticatingRealm
{

  private static final Logger log = LoggerFactory.getLogger(RutAuthRealm.class);

  public static final String ID = "rutauth-realm";

  public static final String DESCRIPTION = "Rut Auth Realm";

  private final SecuritySystem securitySystem;

  @Inject
  public RutAuthRealm(SecuritySystem securitySystem) {
    this.securitySystem = checkNotNull(securitySystem, "securitySystem");
    setName(ID);
    // Any credentials will be a match as we only get the principal
    setCredentialsMatcher(new CredentialsMatcher()
    {
      @Override
      public boolean doCredentialsMatch(final AuthenticationToken token, final AuthenticationInfo info) {
        return true;
      }
    });
  }

  @Override
  public boolean supports(final AuthenticationToken token) {
    return token instanceof RutAuthAuthenticationToken;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws AuthenticationException {
    final Set<User> users = securitySystem.searchUsers(new UserSearchCriteria(token.getPrincipal().toString()));
    if (users != null) {
      SimplePrincipalCollection principals = new SimplePrincipalCollection(token.getPrincipal(), ID);
      for (User user : users) {
        principals.add(user.getUserId(), user.getSource());
      }
      SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(principals, null);
      log.debug(
          "Found principals: '{}' from realms '{}'",
          authenticationInfo.getPrincipals(), authenticationInfo.getPrincipals().getRealmNames()
      );
      return authenticationInfo;
    }
    return null;
  }

}

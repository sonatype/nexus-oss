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
package com.sonatype.nexus.repository.nuget.internal.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.repository.nuget.security.NugetApiKey;
import com.sonatype.nexus.repository.nuget.security.NugetApiKeyStore;

import org.sonatype.nexus.security.UserPrincipalsHelper;
import org.sonatype.nexus.security.authc.NexusApiKeyAuthenticationToken;
import org.sonatype.nexus.security.user.UserNotFoundException;
import org.sonatype.nexus.security.user.UserStatus;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.sisu.Description;

/**
 * {@link AuthenticatingRealm} that maps NuGet API-Keys to valid {@link Subject}s.
 *
 * @since 3.0
 */
@Named(NugetApiKey.NAME)
@Singleton
@Description("NuGet API-Key Realm")
public final class NugetApiKeyRealm
    extends AuthenticatingRealm
{
  public static final String ID = "NuGet-Realm";

  @Inject
  private NugetApiKeyStore keyStore;

  @Inject
  private UserPrincipalsHelper principalsHelper;

  @Override
  public String getName() {
    return NugetApiKey.NAME;
  }

  @Override
  public boolean supports(final AuthenticationToken token) {
    return token instanceof NexusApiKeyAuthenticationToken && NugetApiKey.NAME.equals(token.getPrincipal());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token)
      throws AuthenticationException
  {
    final PrincipalCollection principals = keyStore.getPrincipals((char[]) token.getCredentials());
    if (null != principals) {
      try {
        if (UserStatus.active.equals(principalsHelper.getUserStatus(principals))) {
          ((NexusApiKeyAuthenticationToken) token).setPrincipal(principals.getPrimaryPrincipal());
          return new SimpleAuthenticationInfo(principals, token.getCredentials());
        }
      }
      catch (final UserNotFoundException e) {
        keyStore.deleteApiKey(principals);
      }
    }
    return null;
  }
}

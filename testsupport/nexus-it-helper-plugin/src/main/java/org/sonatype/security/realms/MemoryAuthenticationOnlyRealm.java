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

package org.sonatype.security.realms;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This is a sample of how you can inject your own authentication system
 * but still leave the authorization to nexus.
 *
 * This MemoryAuthenticationOnlyRealm will handle authentication on its own
 *
 * This class can also be loaded by nexus as either a regular class, or as a
 * plexus component.  In the nexus.xml file, you can remove the default
 * XmlAuthenticatingRealm and add this realm as either
 * org.sonatype.jsecurity.realms.MemoryAuthenticationOnlyRealm, or
 * use the role-hint of MemoryAuthenticationOnlyRealm.
 */
@Component(role = org.apache.shiro.realm.Realm.class, hint = "MemoryAuthenticationOnlyRealm")
public class MemoryAuthenticationOnlyRealm
    extends AuthorizingRealm
{
  // Map containing username/password pairs
  private Map<String, String> authenticationMap = new HashMap<String, String>();

  /**
   * This is where we are building the security model, not that the passwords have
   * been changed from the default nexus security, to make for easy validation
   */
  public MemoryAuthenticationOnlyRealm() {
    // As this is a simple test realm, only using simple credentials
    // just a string compare, no hashing involved
    setCredentialsMatcher(new SimpleCredentialsMatcher());

    authenticationMap.put("admin", "admin321");
    authenticationMap.put("deployment", "deployment321");
    authenticationMap.put("anonymous", "anonymous");
  }

  public String getName() {
    return MemoryAuthenticationOnlyRealm.class.getName();
  }

  /**
   * This method is where the authentication is controlled.  You will receive a
   * token, from which you can retrieve the username.  Then you can lookup in your
   * storage, the credentials for that user, place those in an AuthenticationInfo
   * object and return it, the credential matcher will handle comparing them.
   *
   * @see org.apache.shiro.realm.AuthenticatingRealm#doGetAuthenticationInfo(org.apache.shiro.authc.AuthenticationToken)
   */
  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken arg0)
      throws AuthenticationException
  {
    if (!UsernamePasswordToken.class.isAssignableFrom(arg0.getClass())) {
      return null;
    }

    String username = ((UsernamePasswordToken) arg0).getUsername();

    String password = authenticationMap.get(username);

    if (password == null) {
      throw new AuthenticationException("Invalid username '" + username + "'");
    }

    return new SimpleAuthenticationInfo(username, password, getName());
  }

  /**
   * As this is an authentication only realm, we just return null for authorization
   *
   * @see org.apache.shiro.realm.AuthorizingRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
   */
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
    return null;
  }
}

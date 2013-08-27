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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.Realm;
import org.junit.Test;

public class MemoryRealmTest
    extends AbstractRealmTest
{
  private MemoryRealm realm;

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    realm = (MemoryRealm) lookup(Realm.class, "MemoryRealm");
  }

  @Test
  public void testSuccessfulAuthentication()
      throws Exception
  {
    UsernamePasswordToken upToken = new UsernamePasswordToken("admin", "admin123");

    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);

    String password = (String) ai.getCredentials();

    assertEquals("admin123", password);
  }

  @Test
  public void testFailedAuthentication()
      throws Exception
  {
    UsernamePasswordToken upToken = new UsernamePasswordToken("admin", "badpassword");

    try {
      realm.getAuthenticationInfo(upToken);

      fail("Authentication should have failed");
    }
    catch (AuthenticationException e) {
      // good
    }
  }

  @Test
  public void testAdminAuthorization()
      throws Exception
  {
    UsernamePasswordToken upToken = new UsernamePasswordToken("admin", "admin123");

    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);

    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:status:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:authentication:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:update")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:create")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:update")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:delete")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:create")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:delete")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:update")));

    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("junk")));
  }

  @Test
  public void testAnonymousAuthorization()
      throws Exception
  {
    UsernamePasswordToken upToken = new UsernamePasswordToken("anonymous", "anonymous");

    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);

    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:status:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:authentication:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:update")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:create")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:update")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:delete")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("junk")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:create")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:delete")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:update")));
  }

  @Test
  public void testDeploymentAuthorization()
      throws Exception
  {
    UsernamePasswordToken upToken = new UsernamePasswordToken("deployment", "deployment123");

    AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);

    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:status:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:authentication:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:settings:update")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:create")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:read")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:update")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:repositories:delete")));
    assertFalse(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("junk")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:read")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:create")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:delete")));
    assertTrue(realm.isPermitted(ai.getPrincipals(), new WildcardPermission("nexus:target:1:somerepo:update")));
  }
}

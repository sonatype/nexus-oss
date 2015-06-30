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
package org.sonatype.nexus.ldap.internal.realms;

import org.sonatype.nexus.ldap.internal.LdapConstants;
import org.sonatype.nexus.ldap.internal.LdapTestSupport;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.junit.Test;

public class EnterpriseLdapRealmTest
    extends LdapTestSupport
{

  @Test
  public void testSuccessfulAuthentication()
      throws Exception
  {
    final Realm realm = this.lookup(Realm.class, LdapConstants.REALM_NAME);
    final UsernamePasswordToken upToken = new UsernamePasswordToken("brianf", "brianf123");
    final AuthenticationInfo ai = realm.getAuthenticationInfo(upToken);
    assertEquals("brianf123".toCharArray(), ai.getCredentials());
  }
}

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

import javax.naming.ldap.InitialLdapContext;

import org.sonatype.nexus.ldap.internal.LdapITSupport;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class LdapUserDAOIT
    extends LdapITSupport
{
  @Test
  public void testSimple()
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setGroupBaseDn("ou=groups");
    configuration.setGroupObjectClass("groupOfUniqueNames");
    configuration.setGroupMemberAttribute("uniqueMember");
    configuration.setUserRealNameAttribute("cn");

    LdapUserDAO lum = lookup(LdapUserDAO.class);

    LdapUser user = lum.getUser("cstamas", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("cstamas"));
    assertThat(user.getPassword(), equalTo("cstamas123"));

    user = lum.getUser("Fox, Brian", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("Fox, Brian"));
    assertThat(user.getPassword(), equalTo("brianf123"));

    user = lum.getUser("jvanzyl", initialContext, configuration);
    assertThat(user.getUsername(), equalTo("jvanzyl"));
    assertThat(user.getPassword(), equalTo("jvanzyl123"));

    try {
      lum.getUser("intruder", initialContext, configuration);
      fail();
    }
    catch (NoSuchLdapUserException e) {
      // good
    }

    configuration.setLdapFilter("description=nexus");
    // must succeed because cstamas has the attribute description set to nexus
    lum.getUser("cstamas", initialContext, configuration);
    try {
      // must fail because of the ldapFilter that jvanzyl user don't have
      lum.getUser("jvanzyl", initialContext, configuration);
      fail();
    }
    catch (NoSuchLdapUserException e) {
      // good
    }
  }
}

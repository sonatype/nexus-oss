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

import java.util.Set;

import javax.naming.ldap.InitialLdapContext;

import org.sonatype.nexus.ldap.internal.LdapITSupport;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LdapGroupDAOIT
    extends LdapITSupport
{
  @Test
  public void testSimple()
      throws Exception
  {
    doTestWithGroupMemberFormat("cn=${username},ou=people,o=sonatype");
  }

  @Test
  public void testUsingDNInGroupMemberFormat()
      throws Exception
  {
    doTestWithGroupMemberFormat("${dn}");
  }

  protected void doTestWithGroupMemberFormat(String groupMemberFormat)
      throws Exception
  {
    InitialLdapContext initialContext = new InitialLdapContext(initialLdapEnvironment(), null);

    LdapAuthConfiguration configuration = new LdapAuthConfiguration();
    configuration.setUserBaseDn("ou=people");
    configuration.setGroupBaseDn("ou=groups");
    configuration.setGroupObjectClass("groupOfUniqueNames");
    configuration.setGroupMemberAttribute("uniqueMember");
    configuration.setGroupMemberFormat(groupMemberFormat);
    configuration.setLdapGroupsAsRoles(true);
    configuration.setUserMemberOfAttribute("");

    LdapGroupDAO lgm = lookup(LdapGroupDAO.class);

    Set<String> groups = lgm.getGroupMembership("cstamas", initialContext, configuration);
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("snapshots"));

    groups = lgm.getGroupMembership("Fox, Brian", initialContext, configuration);
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("releases"));

    groups = lgm.getGroupMembership("jvanzyl", initialContext, configuration);
    assertTrue(groups.contains("public"));
    assertTrue(groups.contains("releases"));
    assertTrue(groups.contains("snapshots"));
  }
}

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
package org.sonatype.nexus.ldap.internal.templates;

import java.util.List;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultLdapSchemaTemplateManager}.
 */
public class LdapSchemaTemplateManagerTest
    extends TestSupport
{
  private LdapSchemaTemplateManager underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new DefaultLdapSchemaTemplateManager();
  }

  @Test
  public void testLoading() throws Exception {
    List<LdapSchemaTemplate> templates = underTest.getSchemaTemplates();

    Assert.assertNotNull(templates);
    Assert.assertEquals(4, templates.size());

    // get the 2nd one and validate it

    LdapSchemaTemplate template = templates.get(1);
    Assert.assertEquals("Posix with Static Groups", template.getName());
    Assert.assertEquals("mail", template.getUserAndGroupAuthConfig().getEmailAddressAttribute());
    Assert.assertEquals("ou=groups", template.getUserAndGroupAuthConfig().getGroupBaseDn());
    Assert.assertEquals("cn", template.getUserAndGroupAuthConfig().getGroupIdAttribute());
    Assert.assertEquals("memberUid", template.getUserAndGroupAuthConfig().getGroupMemberAttribute());
    Assert.assertEquals("${username}", template.getUserAndGroupAuthConfig().getGroupMemberFormat());
    Assert.assertEquals("posixGroup", template.getUserAndGroupAuthConfig().getGroupObjectClass());
    Assert.assertEquals("ou=people", template.getUserAndGroupAuthConfig().getUserBaseDn());
    Assert.assertEquals("uid", template.getUserAndGroupAuthConfig().getUserIdAttribute());
    Assert.assertEquals(null, template.getUserAndGroupAuthConfig().getUserMemberOfAttribute());
    Assert.assertEquals("posixAccount", template.getUserAndGroupAuthConfig().getUserObjectClass());
    Assert.assertEquals(null, template.getUserAndGroupAuthConfig().getUserPasswordAttribute());
    Assert.assertEquals("cn", template.getUserAndGroupAuthConfig().getUserRealNameAttribute());
    Assert.assertEquals(false, template.getUserAndGroupAuthConfig().isGroupSubtree());
    Assert.assertEquals(true, template.getUserAndGroupAuthConfig().isLdapGroupsAsRoles());
    Assert.assertEquals(false, template.getUserAndGroupAuthConfig().isUserSubtree());
  }
}

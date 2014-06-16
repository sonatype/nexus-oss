/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.security.ldap.api;

import java.util.List;

import com.sonatype.security.ldap.api.dto.LdapSchemaTemplateDTO;
import com.sonatype.security.ldap.api.dto.LdapSchemaTemplateListResponse;

import org.sonatype.plexus.rest.resource.PlexusResource;

import org.junit.Assert;
import org.junit.Test;

public class TemplatesRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapSchemaTempleListPlexusResource");
    Assert.assertFalse(pr.isModifiable());
  }

  @Test
  public void testLoading()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapSchemaTempleListPlexusResource");

    LdapSchemaTemplateListResponse templateResponse = (LdapSchemaTemplateListResponse) pr.get(null, null, null, null);

    List<LdapSchemaTemplateDTO> templates = templateResponse.getData();

    Assert.assertNotNull(templates);
    Assert.assertEquals(4, templates.size());

    // get the 2nd one and validate it

    LdapSchemaTemplateDTO template = templates.get(3);
    Assert.assertEquals("Generic Ldap Server", template.getName());
    Assert.assertEquals("mail", template.getUserAndGroupConfig().getEmailAddressAttribute());
    Assert.assertEquals(null, template.getUserAndGroupConfig().getGroupBaseDn());
    Assert.assertEquals(null, template.getUserAndGroupConfig().getGroupIdAttribute());
    Assert.assertEquals(null, template.getUserAndGroupConfig().getGroupMemberAttribute());
    Assert.assertEquals(null, template.getUserAndGroupConfig().getGroupMemberFormat());
    Assert.assertEquals(null, template.getUserAndGroupConfig().getGroupObjectClass());

    Assert.assertEquals(null, template.getUserAndGroupConfig().getUserBaseDn());
    Assert.assertEquals("uid", template.getUserAndGroupConfig().getUserIdAttribute());
    Assert.assertEquals("memberOf", template.getUserAndGroupConfig().getUserMemberOfAttribute());
    Assert.assertEquals("inetOrgPerson", template.getUserAndGroupConfig().getUserObjectClass());
    Assert.assertEquals("userPassword", template.getUserAndGroupConfig().getUserPasswordAttribute());
    Assert.assertEquals("cn", template.getUserAndGroupConfig().getUserRealNameAttribute());
    Assert.assertEquals(false, template.getUserAndGroupConfig().isGroupSubtree());
    Assert.assertEquals(true, template.getUserAndGroupConfig().isLdapGroupsAsRoles());
    Assert.assertEquals(false, template.getUserAndGroupConfig().isUserSubtree());
  }

}

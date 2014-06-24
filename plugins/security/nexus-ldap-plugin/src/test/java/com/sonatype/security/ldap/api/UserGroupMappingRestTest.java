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

import java.util.HashMap;
import java.util.Map;

import com.sonatype.security.ldap.AbstractLdapTestCase;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapUserDTO;
import com.sonatype.security.ldap.api.dto.LdapUserListResponse;

import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.sisu.litmus.testsupport.group.Slow;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

@Category(Slow.class)
public class UserGroupMappingRestTest
    extends AbstractLdapTestCase
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapUserAndGroupConfigTestPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testSuccess()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapUserAndGroupConfigTestPlexusResource");

    LdapServerRequest ldapServerRequest = new LdapServerRequest();
    ldapServerRequest.setData(new LdapServerConfigurationDTO());

    ldapServerRequest.getData().setName("testSuccess");

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerRequest.getData().setConnectionInfo(connDto);
    connDto.setAuthScheme("simple");
    connDto.setHost("localhost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    connDto.setSystemPassword(encodeBase64("secret"));
    connDto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerRequest.getData().setUserAndGroupConfig(userGroupDto);

    userGroupDto.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    userGroupDto.setGroupObjectClass("organizationalRole");
    userGroupDto.setGroupBaseDn("ou=groups");
    userGroupDto.setGroupIdAttribute("cn");
    userGroupDto.setGroupMemberAttribute("uniqueMember");
    userGroupDto.setUserObjectClass("inetOrgPerson");
    userGroupDto.setUserBaseDn("ou=people");
    userGroupDto.setUserIdAttribute("uid");
    userGroupDto.setUserPasswordAttribute("userPassword");
    userGroupDto.setUserRealNameAttribute("sn");
    userGroupDto.setUserMemberOfAttribute("businesscategory");
    userGroupDto.setEmailAddressAttribute("mail");
    userGroupDto.setLdapGroupsAsRoles(true);

    Request request = new Request();
    Response response = new Response(request);

    LdapUserListResponse userListResponse = (LdapUserListResponse) pr.put(
        null,
        request,
        response,
        ldapServerRequest);

    Assert.assertEquals(3, userListResponse.getData().size());

    // build a nice little map so we can test things without a else if
    Map<String, LdapUserDTO> userMap = new HashMap<String, LdapUserDTO>();
    for (LdapUserDTO user : userListResponse.getData()) {
      userMap.put(user.getUserId(), user);
    }

    // now check everybody
    LdapUserDTO cstamas = userMap.get("cstamas");
    Assert.assertEquals("Tamas Cservenak", cstamas.getName());
    Assert.assertEquals("cstamas@sonatype.com", cstamas.getEmail());
    Assert.assertEquals(2, cstamas.getRoles().size());
    Assert.assertTrue(cstamas.getRoles().contains("public"));
    Assert.assertTrue(cstamas.getRoles().contains("snapshots"));

    LdapUserDTO brianf = userMap.get("brianf");
    Assert.assertEquals("Brian Fox", brianf.getName());
    Assert.assertEquals("brianf@sonatype.com", brianf.getEmail());
    Assert.assertEquals(2, brianf.getRoles().size());
    Assert.assertTrue(brianf.getRoles().contains("public"));
    Assert.assertTrue(brianf.getRoles().contains("releases"));

    LdapUserDTO jvanzyl = userMap.get("jvanzyl");
    Assert.assertEquals("Jason Van Zyl", jvanzyl.getName());
    Assert.assertEquals("jvanzyl@sonatype.com", jvanzyl.getEmail());
    Assert.assertEquals(3, jvanzyl.getRoles().size());
    Assert.assertTrue(jvanzyl.getRoles().contains("public"));
    Assert.assertTrue(jvanzyl.getRoles().contains("snapshots"));
    Assert.assertTrue(jvanzyl.getRoles().contains("releases"));

  }

  @Test
  public void testFailure()
      throws Exception
  {

    PlexusResource pr = this.lookup(PlexusResource.class, "LdapUserAndGroupConfigTestPlexusResource");

    LdapServerRequest ldapServerRequest = new LdapServerRequest();
    ldapServerRequest.setData(new LdapServerConfigurationDTO());

    ldapServerRequest.getData().setName("testFailure");

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerRequest.getData().setConnectionInfo(connDto);
    connDto.setAuthScheme("simple");
    connDto.setHost("invalidHost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    connDto.setSystemPassword(encodeBase64("secret"));
    connDto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerRequest.getData().setUserAndGroupConfig(userGroupDto);

    userGroupDto.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    userGroupDto.setGroupObjectClass("organizationalRole");
    userGroupDto.setGroupBaseDn("ou=groups");
    userGroupDto.setGroupIdAttribute("cn");
    userGroupDto.setGroupMemberAttribute("uniqueMember");
    userGroupDto.setUserObjectClass("inetOrgPerson");
    userGroupDto.setUserBaseDn("ou=people");
    userGroupDto.setUserIdAttribute("uid");
    userGroupDto.setUserPasswordAttribute("userPassword");
    userGroupDto.setUserRealNameAttribute("sn");
    userGroupDto.setUserMemberOfAttribute("businesscategory");
    userGroupDto.setEmailAddressAttribute("mail");
    userGroupDto.setLdapGroupsAsRoles(true);

    Request request = new Request();
    Response response = new Response(request);

    try {
      pr.put(null, request, response, ldapServerRequest);
      Assert.fail("expected ResourceException");
    }
    catch (ResourceException e) {
      // expected
    }

  }
}

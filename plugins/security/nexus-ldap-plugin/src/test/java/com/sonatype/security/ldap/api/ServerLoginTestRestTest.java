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

import com.sonatype.security.ldap.AbstractLdapTestCase;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.plexus.rest.resource.PlexusResource;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

public class ServerLoginTestRestTest
    extends AbstractLdapTestCase
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testReadable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");
    Assert.assertFalse(pr.isReadable());
  }

  @Test
  public void testInvalidLogin()
      throws Exception
  {
    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connDto);
    connDto.setAuthScheme("simple");
    connDto.setHost("localhost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    connDto.setSystemPassword(encodeBase64("secret"));
    connDto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupDto);

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

    ldapServerLoginTestRequest.getData().setUsername(
        encodeBase64("brianf")
    );
    ldapServerLoginTestRequest.getData().setPassword(
        encodeBase64("JUNK")
    );

    Request request = this.buildRequest();
    Response response = new Response(request);
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");

    try {
      pr.put(null, request, response, ldapServerLoginTestRequest);
      Assert.fail("expected ResourceException");
    }
    catch (ResourceException e) {
      Assert.assertEquals(400, e.getStatus().getCode());
    }
  }

  @Test
  public void testInvalidLoginWithBind()
      throws Exception
  {

    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connDto);
    connDto.setAuthScheme("simple");
    connDto.setHost("localhost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    connDto.setSystemPassword(encodeBase64("secret"));
    connDto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupDto);

    userGroupDto.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    userGroupDto.setGroupObjectClass("organizationalRole");
    userGroupDto.setGroupBaseDn("ou=groups");
    userGroupDto.setGroupIdAttribute("cn");
    userGroupDto.setGroupMemberAttribute("uniqueMember");
    userGroupDto.setUserObjectClass("inetOrgPerson");
    userGroupDto.setUserBaseDn("ou=people");
    userGroupDto.setUserIdAttribute("uid");
    userGroupDto.setUserPasswordAttribute("");
    userGroupDto.setUserRealNameAttribute("sn");
    userGroupDto.setUserMemberOfAttribute("businesscategory");
    userGroupDto.setEmailAddressAttribute("mail");
    userGroupDto.setLdapGroupsAsRoles(true);

    ldapServerLoginTestRequest.getData().setUsername(encodeBase64("brianf"));
    ldapServerLoginTestRequest.getData().setPassword(encodeBase64("JUNK"));

    Request request = this.buildRequest();
    Response response = new Response(request);
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");

    try {
      pr.put(null, request, response, ldapServerLoginTestRequest);
      Assert.fail("expected ResourceException");
    }
    catch (ResourceException e) {
      Assert.assertEquals(400, e.getStatus().getCode());
    }
  }

  @Test
  public void testLogin()
      throws Exception
  {

    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connDto);
    connDto.setAuthScheme("simple");
    connDto.setHost("localhost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    connDto.setSystemPassword(encodeBase64("secret"));
    connDto.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupDto);

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

    ldapServerLoginTestRequest.getData().setUsername(encodeBase64("brianf"));
    ldapServerLoginTestRequest.getData().setPassword(encodeBase64("brianf123"));

    Request request = this.buildRequest();
    Response response = new Response(request);
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");

    pr.put(null, request, response, ldapServerLoginTestRequest);

    Assert.assertEquals(204, response.getStatus().getCode());
  }

  @Test
  public void testLoginWithBind()
      throws Exception
  {

    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    LdapConnectionInfoDTO connDto = new LdapConnectionInfoDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connDto);
    connDto.setAuthScheme("none");
    connDto.setHost("localhost");
    connDto.setPort(this.getLdapServer("default").getPort());
    connDto.setProtocol("ldap");
    //        connDto.setSystemPassword( "secret" );
    //        connDto.setSystemUsername( "uid=admin,ou=system" );
    connDto.setSearchBase("o=sonatype");

    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupDto);

    userGroupDto.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    userGroupDto.setGroupObjectClass("organizationalRole");
    userGroupDto.setGroupBaseDn("ou=groups");
    userGroupDto.setGroupIdAttribute("cn");
    userGroupDto.setGroupMemberAttribute("uniqueMember");
    userGroupDto.setUserObjectClass("inetOrgPerson");
    userGroupDto.setUserBaseDn("ou=people");
    userGroupDto.setUserIdAttribute("uid");
    userGroupDto.setUserPasswordAttribute("");
    userGroupDto.setUserRealNameAttribute("sn");
    userGroupDto.setUserMemberOfAttribute("businesscategory");
    userGroupDto.setEmailAddressAttribute("mail");
    userGroupDto.setLdapGroupsAsRoles(true);

    ldapServerLoginTestRequest.getData().setUsername(encodeBase64("brianf"));
    ldapServerLoginTestRequest.getData().setPassword(encodeBase64("brianf123"));

    Request request = this.buildRequest();
    Response response = new Response(request);
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerLoginTestPlexusResource");

    pr.put(null, request, response, ldapServerLoginTestRequest);

    Assert.assertEquals(204, response.getStatus().getCode());
  }

  private Request buildRequest() {
    Request request = new Request();
    request.setRootRef(new Reference("http://localhost:12345/"));
    request.setResourceRef(new Reference("http://localhost:12345/ldap/logintest"));
    return request;
  }

}

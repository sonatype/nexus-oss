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

import java.util.ArrayList;
import java.util.List;

import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListEntryDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;

import org.sonatype.plexus.rest.resource.PlexusResource;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Request;

public class ServerListRestTest
    extends AbstractLdapRestTest
{

  @Test
  public void testModifiable()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerListPlexusResource");
    Assert.assertTrue(pr.isModifiable());
  }

  @Test
  public void testPost()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerListPlexusResource");

    LdapServerRequest ldapServerRequest = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    ldapServerRequest.setData(dto);
    // dto.setId( "testPost" ); // this will be generated
    dto.setName("Test Post");
    dto.setConnectionInfo(this.buildConnectionInfoDTO());
    dto.setUserAndGroupConfig(this.buildUserAndGroupAuthConfigurationDTO());

    Request request = this.buildRequest();
    LdapServerRequest postResult = (LdapServerRequest) pr.post(null, request, null, ldapServerRequest);

    // we need to update the ID in the original, because it was set on the server
    Assert.assertNotNull(postResult.getData().getId());
    dto.setId(postResult.getData().getId());

    // update the request with the expected URL, so we can compare
    dto.setUrl(request.getResourceRef().toString() + "/" + dto.getId());

    dto.getConnectionInfo().setSystemPassword(encodeBase64((AbstractLdapPlexusResource.FAKE_PASSWORD)));
    this.compare(dto, postResult.getData());
  }

  @Test
  public void testPost2()
      throws Exception
  {
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerListPlexusResource");

    LdapServerRequest ldapServerRequest = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    LdapConnectionInfoDTO connectionDto = new LdapConnectionInfoDTO();
    LdapUserAndGroupAuthConfigurationDTO userGroupDto = new LdapUserAndGroupAuthConfigurationDTO();

    ldapServerRequest.setData(dto);
    dto.setConnectionInfo(connectionDto);
    dto.setUserAndGroupConfig(userGroupDto);

    dto.setName("test");
    connectionDto.setHost("test");
    connectionDto.setPort(389);
    connectionDto.setSearchBase("test");
    connectionDto.setAuthScheme("none");
    connectionDto.setProtocol("ldap");
    connectionDto.setBackupMirrorProtocol("ldap");
    connectionDto.setBackupMirrorHost("asdf");
    connectionDto.setBackupMirrorPort(389);
    connectionDto.setConnectionTimeout(33);
    connectionDto.setConnectionRetryDelay(44);
    connectionDto.setCacheTimeout(55);
    userGroupDto.setEmailAddressAttribute("qasdf");
    userGroupDto.setLdapGroupsAsRoles(true);
    userGroupDto.setUserPasswordAttribute("asdf");
    userGroupDto.setUserIdAttribute("asdf");
    userGroupDto.setUserObjectClass("asdf");
    userGroupDto.setUserRealNameAttribute("asdf");
    userGroupDto.setUserSubtree(false);
    userGroupDto.setGroupSubtree(false);
    userGroupDto.setUserMemberOfAttribute("asdf");

    Request request = this.buildRequest();

    LdapServerRequest postResult = (LdapServerRequest) pr.post(null, request, null, ldapServerRequest);

    // we need to update the ID in the original, because it was set on the server
    Assert.assertNotNull(postResult.getData().getId());
    dto.setId(postResult.getData().getId());

    // update the request with the expected URL, so we can compare
    dto.setUrl(request.getResourceRef().toString() + "/" + dto.getId());

    this.compare(dto, postResult.getData());
  }

  @Test
  public void testGet()
      throws Exception
  {
    // delete the one generated by the test class
    this.lookup(LdapConfigurationManager.class).deleteLdapServerConfiguration("default");

    List<String> expectedIds = new ArrayList<String>();
    PlexusResource pr = this.lookup(PlexusResource.class, "LdapServerListPlexusResource");

    Request request = this.buildRequest();

    // ONE
    LdapServerRequest ldapServerRequest = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    ldapServerRequest.setData(dto);
    dto.setName("testGet-1");
    dto.setConnectionInfo(this.buildConnectionInfoDTO());
    dto.getConnectionInfo().setHost("testGet1");
    dto.setUserAndGroupConfig(this.buildUserAndGroupAuthConfigurationDTO());

    LdapServerRequest postResult = (LdapServerRequest) pr.post(null, request, null, ldapServerRequest);
    Assert.assertNotNull(postResult.getData().getId());
    expectedIds.add(postResult.getData().getId());

    // TWO
    ldapServerRequest = new LdapServerRequest();
    dto = new LdapServerConfigurationDTO();
    ldapServerRequest.setData(dto);
    dto.setName("testGet-2");
    dto.setConnectionInfo(this.buildConnectionInfoDTO());
    dto.getConnectionInfo().setPort(7788);
    dto.setUserAndGroupConfig(this.buildUserAndGroupAuthConfigurationDTO());

    postResult = (LdapServerRequest) pr.post(null, request, null, ldapServerRequest);
    Assert.assertNotNull(postResult.getData().getId());
    expectedIds.add(postResult.getData().getId());

    // THREE
    ldapServerRequest = new LdapServerRequest();
    dto = new LdapServerConfigurationDTO();
    ldapServerRequest.setData(dto);
    dto.setName("testGet-3");
    dto.setConnectionInfo(this.buildConnectionInfoDTO());
    dto.getConnectionInfo().setSearchBase("ou=testGet3");
    dto.setUserAndGroupConfig(this.buildUserAndGroupAuthConfigurationDTO());

    postResult = (LdapServerRequest) pr.post(null, request, null, ldapServerRequest);
    Assert.assertNotNull(postResult.getData().getId());
    expectedIds.add(postResult.getData().getId());

    // now test get
    LdapServerListResponse listResponse = (LdapServerListResponse) pr.get(null, request, null, null);
    List<LdapServerListEntryDTO> results = listResponse.getData();

    //these should be in order as they where created
    Assert.assertEquals(expectedIds.get(0), results.get(0).getId());
    Assert.assertEquals("testGet-1", results.get(0).getName());
    Assert.assertEquals("ldap://testGet1:386/ou=searchbase", results.get(0).getLdapUrl());
    Assert.assertEquals("http://localhost:12345/ldap/servers/" + results.get(0).getId(), results.get(0).getUrl());


    // should only be 3 results
    Assert.assertEquals(3, results.size());

  }
}

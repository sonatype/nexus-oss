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
package com.sonatype.nexus.testsuite.ldap.nxcm1356;

import java.io.IOException;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListEntryDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;

public class NXCM1356ServerListIT
    extends AbstractLdapIT
{

  protected boolean isStartServer() {
    return false;
  }

  @Test
  public void testPost()
      throws Exception
  {
    this.createServer();
  }

  @Test
  public void testGet()
      throws Exception
  {
    // call post to create data
    LdapServerRequest ldapResponse = this.createServer();
    Response response = null;
    try {
      response = RequestFacade.doGetRequest(RequestFacade.SERVICE_LOCAL + "ldap/servers");
      LdapServerListResponse listResponse = this.getFromResponse(
          LdapServerListResponse.class,
          this.getXMLXStream(),
          response);

      boolean found = false;
      for (LdapServerListEntryDTO entry : listResponse.getData()) {
        if (entry.getId().equals(ldapResponse.getData().getId())) {
          found = true;
          break;
        }
      }
      Assert.assertTrue("Server with ID: " + ldapResponse.getData().getId() + " was not found in list", found);

    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private LdapServerRequest createServer()
      throws Exception
  {
    LdapServerRequest serverRequest = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    serverRequest.setData(dto);

    dto.setId(null); // not set
    dto.setName("testPost");
    dto.setUrl(null); // set on the return, not the request

    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();
    dto.setConnectionInfo(connInfo);

    connInfo.setAuthScheme("ldap");
    connInfo.setBackupMirrorHost("backupHost");
    connInfo.setBackupMirrorPort(11111);
    connInfo.setBackupMirrorProtocol("ldap");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost("localhost");
    connInfo.setPort(386);
    connInfo.setProtocol("ldap");
    connInfo.setRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));

    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();
    dto.setUserAndGroupConfig(userGroupConf);

    userGroupConf.setUserMemberOfAttribute("userMemberOfAttribute");
    userGroupConf.setGroupBaseDn("groupBaseDn");
    userGroupConf.setGroupIdAttribute("groupIdAttribute");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("groupMemberFormat");
    userGroupConf.setGroupObjectClass("groupObjectClass");
    userGroupConf.setLdapGroupsAsRoles(true);

    userGroupConf.setEmailAddressAttribute("emailAddressAttribute");
    userGroupConf.setUserBaseDn("userBaseDn");
    userGroupConf.setUserIdAttribute("userIdAttribute");
    userGroupConf.setUserObjectClass("userObjectClass");
    userGroupConf.setUserPasswordAttribute("userPasswordAttribute");
    userGroupConf.setUserRealNameAttribute("userRealNameAttribute");
    userGroupConf.setUserSubtree(true);

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/servers",
          Method.POST,
          new XStreamRepresentation(xstream, xstream.toXML(serverRequest), MediaType.APPLICATION_XML));

      Assert.assertEquals(
          "Expected status 201, found: " + response.getStatus(),
          201,
          response.getStatus().getCode());

      LdapServerRequest ldapResponse = this.getFromResponse(LdapServerRequest.class, xstream, response);
      Assert.assertNotNull(ldapResponse);
      Assert.assertNotNull(ldapResponse.getData().getId());

      return ldapResponse;
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  @Test
  public void testPostFail()
      throws IOException
  {
    LdapServerRequest serverRequest = new LdapServerRequest();
    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    serverRequest.setData(dto);

    dto.setId(null); // not set
    dto.setName("testPost");
    dto.setUrl(null); // set on the return, not the request

    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();
    dto.setConnectionInfo(connInfo);

    connInfo.setAuthScheme("ldap");
    connInfo.setBackupMirrorHost("backupHost");
    connInfo.setBackupMirrorPort(11111);
    connInfo.setBackupMirrorProtocol("ldap");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(300);
    connInfo.setConnectionTimeout(15);
    connInfo.setHost(null); // missing host
    connInfo.setPort(386);
    connInfo.setProtocol("ldap");
    connInfo.setRealm("");
    connInfo.setSearchBase("ou=searchbase");
    connInfo.setSystemPassword(encodeBase64("systemPassword"));
    connInfo.setSystemUsername(encodeBase64("systemUsername"));

    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();
    dto.setUserAndGroupConfig(userGroupConf);

    userGroupConf.setUserMemberOfAttribute("userMemberOfAttribute");
    userGroupConf.setGroupBaseDn("groupBaseDn");
    userGroupConf.setGroupIdAttribute("groupIdAttribute");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("groupMemberFormat");
    userGroupConf.setGroupObjectClass("groupObjectClass");
    userGroupConf.setLdapGroupsAsRoles(true);

    userGroupConf.setEmailAddressAttribute("emailAddressAttribute");
    userGroupConf.setUserBaseDn("userBaseDn");
    userGroupConf.setUserIdAttribute("userIdAttribute");
    userGroupConf.setUserObjectClass("userObjectClass");
    userGroupConf.setUserPasswordAttribute("userPasswordAttribute");
    userGroupConf.setUserRealNameAttribute("userRealNameAttribute");
    userGroupConf.setUserSubtree(true);

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/servers",
          Method.POST,
          new XStreamRepresentation(xstream, xstream.toXML(serverRequest), MediaType.APPLICATION_XML));

      Assert
          .assertEquals("Expected status 200, found: " + response.getStatus(), 400, response.getStatus().getCode());
      String responseText = response.getEntity().getText();
      Assert.assertTrue(
          "Expected response to contain error message containing: host\n" + responseText,
          responseText.contains("host"));
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }
}

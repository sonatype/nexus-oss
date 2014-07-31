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
package com.sonatype.nexus.testsuite.ldap.nexus4062;

import java.util.List;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListEntryDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/**
 * Test for NEXUS-4062: LDAP realm is activated when properly configured.
 *
 * @author cstamas
 * @see <a href="https://issues.sonatype.org/browse/NEXUS-4062">NEXUS-4062</a>
 * @since 2.7.0
 */
public class NEXUS4062LdapActivatedWhenConfiguredIT
    extends AbstractLdapIT
{
  /**
   * No need for local LDAP server.
   */
  @Override
  protected boolean isStartServer() {
    return false;
  }

  @Test
  public void ldapIsActivatedWhenConfigured() throws Exception {
    // default config will have Server entry upgraded from OSS configuration, so we need to get rid of it
    removeServers();

    // now we are in state to begin the test
    // capture active realms before adding 1st server
    final List<String> activeRealmsBeforeConf = SettingsMessageUtil.getCurrentSettings().getSecurityRealms();
    // add a dummy server
    createServer();
    // capture active realms after adding 1st server
    final List<String> activeRealmsAfterConf = SettingsMessageUtil.getCurrentSettings().getSecurityRealms();

    // at starting point LDAP Realm was not active
    assertThat(activeRealmsBeforeConf, not(hasItem("LdapAuthenticatingRealm")));
    // when configured, LDAP Realm was automatically added to active Realms
    assertThat(activeRealmsAfterConf, hasItem("LdapAuthenticatingRealm"));
  }

  private void removeServers() throws Exception {
    XStream xstream = getXMLXStream();
    Response response = null;
    try {
      // get all server entries, as we don't know the ID of them, since it was OSS config upgraded on the fly
      response = RequestFacade.sendMessage(RequestFacade.SERVICE_LOCAL + "ldap/servers", Method.GET);
      Assert.assertEquals("Expected status 200, got: " + response.getStatus(), 200, response.getStatus().getCode());
      LdapServerListResponse ldapServers = getFromResponse(LdapServerListResponse.class, xstream, response);
      Assert.assertNotNull(ldapServers);

      // delete each entry we find
      for (LdapServerListEntryDTO serverEntry : ldapServers.getData()) {
        response = RequestFacade.sendMessage(RequestFacade.SERVICE_LOCAL + "ldap/servers/" + serverEntry.getId(),
            Method.DELETE);
        Assert.assertEquals("Expected status 204, got: " + response.getStatus(), 204, response.getStatus().getCode());
      }
    }
    finally {
      RequestFacade.releaseResponse(response);
    }

  }

  private LdapServerRequest createServer() throws Exception {
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

    XStream xstream = getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(RequestFacade.SERVICE_LOCAL + "ldap/servers", Method.POST,
          new XStreamRepresentation(xstream, xstream.toXML(serverRequest), MediaType.APPLICATION_XML));

      Assert.assertEquals("Expected status 201, found: " + response.getStatus(), 201, response.getStatus().getCode());

      LdapServerRequest ldapResponse = getFromResponse(LdapServerRequest.class, xstream, response);
      Assert.assertNotNull(ldapResponse);
      Assert.assertNotNull(ldapResponse.getData().getId());

      return ldapResponse;
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }
}

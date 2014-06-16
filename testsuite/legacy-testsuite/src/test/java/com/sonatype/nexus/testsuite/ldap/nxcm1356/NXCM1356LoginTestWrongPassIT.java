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

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;

public class NXCM1356LoginTestWrongPassIT
    extends AbstractLdapIT
{

  @Test
  public void testPutInvalidPass()
      throws Exception
  {
    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    ldapServerLoginTestRequest.getData().getConfiguration().setId(null); // not set
    ldapServerLoginTestRequest.getData().getConfiguration().setName("testPost");
    ldapServerLoginTestRequest.getData().getConfiguration().setUrl(null); // set on the return, not the request

    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connInfo);

    connInfo.setAuthScheme("simple");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(0);
    connInfo.setConnectionTimeout(10);
    connInfo.setHost("localhost");
    connInfo.setPort(this.getLdapServer().getPort());
    connInfo.setProtocol("ldap");
    // connInfo.setRealm( "" );
    connInfo.setSearchBase("o=sonatype");
    connInfo.setSystemPassword(encodeBase64("secret"));
    connInfo.setSystemUsername(encodeBase64("uid=admin,ou=system"));

    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupConf);

    userGroupConf.setUserMemberOfAttribute("businesscategory");
    userGroupConf.setGroupBaseDn("ou=groups");
    userGroupConf.setGroupIdAttribute("cn");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("cn=${username},ou=groups,o=sonatype");
    userGroupConf.setGroupObjectClass("organizationalRole");
    userGroupConf.setLdapGroupsAsRoles(true);

    userGroupConf.setEmailAddressAttribute("mail");
    userGroupConf.setUserBaseDn("ou=people");
    userGroupConf.setUserIdAttribute("uid");
    userGroupConf.setUserObjectClass("inetOrgPerson");
    userGroupConf.setUserPasswordAttribute("userPassword");
    userGroupConf.setUserRealNameAttribute("sn");
    userGroupConf.setUserSubtree(true);

    ldapServerLoginTestRequest.getData().setUsername(encodeBase64("brianf"));
    ldapServerLoginTestRequest.getData().setPassword(encodeBase64("INVALID"));

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/logintest",
          Method.PUT,
          new XStreamRepresentation(
              xstream,
              xstream.toXML(ldapServerLoginTestRequest),
              MediaType.APPLICATION_XML));

      String responseText = response.getEntity().getText();

      Assert.assertEquals(
          "Expected status 400, found: " + response.getStatus() + "\n" + responseText,
          400,
          response.getStatus().getCode());
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }
}

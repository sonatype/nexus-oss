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
package com.sonatype.nexus.testsuite.ldap.nxcm2974;

import java.io.File;

import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.resource.StringRepresentation;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.test.utils.ResponseMatchers.respondsWithStatusCode;

public class NXCM2974DoNotHtmlEscapeUserPassFromRestPayloadIT
    extends AbstractLdapIT
{

  @Test
  public void testSave()
      throws Exception
  {
    TestContainer.getInstance().getTestContext().useAdminForRequests();
    String userName = encodeBase64("cn=user,ou=L&L");
    String password = encodeBase64("a&o");

    String json =
        "{'data':{'name':'test','connectionInfo':{'searchBase':'o=more&more','systemUsername':'" + userName +
            "','systemPassword':'" + password +
            "','authScheme':'simple','protocol':'ldap','host':'localhost','port':1234,'backupMirrorPort':0,'connectionTimeout':30,'connectionRetryDelay':300,'cacheTimeout':600},'userAndGroupConfig':{'emailAddressAttribute':'mail','ldapGroupsAsRoles':true,'userIdAttribute':'uid','userObjectClass':'inetOrgPerson','userBaseDn':'ou=people','userRealNameAttribute':'sn','userSubtree':false,'groupSubtree':false,'userMemberOfAttribute':'businesscategory'}}}";

    String responseText =
        RequestFacade.doPostForText(RequestFacade.SERVICE_LOCAL + "ldap/servers", new StringRepresentation(json,
            MediaType.APPLICATION_JSON), respondsWithStatusCode(201));

    assertThat(responseText, containsString("more&more"));

    responseText =
        RequestFacade.doGetForText(RequestFacade.SERVICE_LOCAL + "ldap/servers", respondsWithStatusCode(200));

    // &amp; is expected because response format is XML
    assertThat(responseText, containsString("more&amp;more"));

    // test for correct ldap config
    File ldapConfigFile = new File(NexusConfigUtil.getNexusFile().getParentFile(), "ldap.xml");
    assertThat("cannot find ldap config", ldapConfigFile.exists());
    assertThat(FileUtils.readFileToString(ldapConfigFile),
        allOf(containsString("L&amp;L"), containsString("more&amp;more")));
  }

  @Test
  public void testLdapLoginTest()
      throws Exception
  {
    LdapServerLoginTestRequest ldapServerLoginTestRequest = new LdapServerLoginTestRequest();
    ldapServerLoginTestRequest.setData(new LdapServerLoginTestDTO());
    ldapServerLoginTestRequest.getData().setConfiguration(new LdapServerConfigurationDTO());

    ldapServerLoginTestRequest.getData().getConfiguration().setId(null); // not set
    ldapServerLoginTestRequest.getData().getConfiguration().setName("test");
    ldapServerLoginTestRequest.getData().getConfiguration().setUrl(null); // set on the return, not the request

    LdapConnectionInfoDTO connInfo = getConnectionInfo();
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = getUserGroupConf();

    ldapServerLoginTestRequest.getData().getConfiguration().setConnectionInfo(connInfo);
    ldapServerLoginTestRequest.getData().getConfiguration().setUserAndGroupConfig(userGroupConf);

    ldapServerLoginTestRequest.getData().setUsername(encodeBase64("brianf"));
    ldapServerLoginTestRequest.getData().setPassword(encodeBase64("brianf123"));

    XStreamRepresentation repr =
        new XStreamRepresentation(this.getXMLXStream(), this.getXMLXStream().toXML(ldapServerLoginTestRequest),
            MediaType.APPLICATION_XML);

    RequestFacade.doPut(RequestFacade.SERVICE_LOCAL + "ldap/logintest", repr, respondsWithStatusCode(204));
  }

  private LdapUserAndGroupAuthConfigurationDTO getUserGroupConf() {
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();
    userGroupConf.setUserMemberOfAttribute("businesscategory");
    userGroupConf.setGroupBaseDn("ou=groups");
    userGroupConf.setGroupIdAttribute("cn");
    userGroupConf.setGroupMemberAttribute("groupMemberAttribute");
    userGroupConf.setGroupMemberFormat("cn=${username},ou=groups,o=more&more");
    userGroupConf.setGroupObjectClass("organizationalRole");
    userGroupConf.setLdapGroupsAsRoles(true);

    userGroupConf.setEmailAddressAttribute("mail");
    userGroupConf.setUserBaseDn("ou=people");
    userGroupConf.setUserIdAttribute("uid");
    userGroupConf.setUserObjectClass("inetOrgPerson");
    userGroupConf.setUserPasswordAttribute("userPassword");
    userGroupConf.setUserRealNameAttribute("sn");
    userGroupConf.setUserSubtree(true);
    return userGroupConf;
  }

  private LdapConnectionInfoDTO getConnectionInfo() {
    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();

    connInfo.setAuthScheme("simple");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(0);
    connInfo.setConnectionTimeout(10);
    connInfo.setHost("localhost");
    connInfo.setPort(this.getLdapServer().getPort());
    connInfo.setProtocol("ldap");
    // connInfo.setRealm( "" );
    connInfo.setSearchBase("o=more&more");
    connInfo.setSystemPassword(encodeBase64("secret"));
    connInfo.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    return connInfo;
  }
}

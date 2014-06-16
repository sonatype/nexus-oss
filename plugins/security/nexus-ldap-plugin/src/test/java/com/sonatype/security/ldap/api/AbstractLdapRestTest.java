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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.sonatype.security.ldap.AbstractMockLdapConnectorTest;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.security.ldap.realms.connector.LdapConnector;

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.restlet.data.Reference;
import org.restlet.data.Request;

public abstract class AbstractLdapRestTest
    extends AbstractMockLdapConnectorTest
{

  protected Request buildRequest(String id) {
    String idPart = id == null ? "" : ("/" + id);

    Request request = new Request();
    request.setRootRef(new Reference("http://localhost:12345/"));
    request.setResourceRef(new Reference("http://localhost:12345/ldap/servers" + idPart));

    if (id != null) {
      request.getAttributes().put("serverId", id);
    }

    return request;
  }

  protected Request buildRequest() {
    return this.buildRequest(null);
  }

  protected LdapConnectionInfoDTO buildConnectionInfoDTO() throws UnsupportedEncodingException {
    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();

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

    return connInfo;
  }

  protected LdapUserAndGroupAuthConfigurationDTO buildUserAndGroupAuthConfigurationDTO() {
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();

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

    return userGroupConf;
  }

  protected void compare(LdapServerConfigurationDTO expected, LdapServerConfigurationDTO actual) {
    XStream xstream = new XStream();

    String expectedString = xstream.toXML(expected);
    String actualString = xstream.toXML(actual);
    Assert.assertEquals(expectedString, actualString);
  }

  protected void compare(LdapServerConfigurationDTO dto, CLdapServerConfiguration ldapServer) {
    LdapRestTestUtils.compare(dto, ldapServer);
  }

  @Override
  protected List<LdapConnector> getLdapConnectors() {
    return new ArrayList<LdapConnector>();
  }

}

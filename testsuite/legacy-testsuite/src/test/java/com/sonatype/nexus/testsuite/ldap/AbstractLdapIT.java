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

package com.sonatype.nexus.testsuite.ldap;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.sonatype.security.ldap.api.dto.XStreamInitalizer;
import com.sonatype.security.ldap.api.dto.LdapConnectionInfoDTO;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import org.apache.shiro.codec.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.restlet.data.Response;

import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;

public abstract class AbstractLdapIT
    extends AbstractNexusIntegrationTest
{
  public final String LDIF_DIR = "../../ldif_dir";

  private static LdapServer ldapServer;

  @BeforeClass
  public static void setSecurity() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Override
  protected void copyConfigFiles() throws IOException {
    super.copyConfigFiles();

    HashMap<String, String> map = new HashMap<String, String>();

    // at this point the ldapServer should be running (if we have one)
    if (this.isStartServer()) {
      this.copyConfigFile("test.ldif", LDIF_DIR);

      try {
        ldapServer = this.lookup(LdapServer.class);
        if (!ldapServer.isStarted()) {
          ldapServer.start();
        }
      }
      catch (Exception e) {
        e.printStackTrace();
        Assert.fail("Could not start or lookup ldap server ");
      }

      map.put("default-ldap-port", Integer.toString(ldapServer.getPort()));
    }
    else {
      map.put("default-ldap-port", Integer.toString(12345));
    }

    // copy ldap.xml to work dir
    this.copyConfigFile("ldap.xml", map, WORK_CONF_DIR);
  }

  protected static boolean deleteLdapConfig() {
    File ldapConfig = new File(WORK_CONF_DIR, "ldap.xml");
    if (ldapConfig.exists()) {
      return ldapConfig.delete();
    }
    return true;
  }

  @AfterClass
  public static void afterLdapTests()
      throws Exception
  {
    if (ldapServer != null) {
      ldapServer.stop();
    }
    ldapServer = null;
  }

  @Override
  public XStream getXMLXStream() {
    return new XStreamInitalizer().initXStream(super.getXMLXStream());
  }

  @Override
  public XStream getJsonXStream() {
    return new XStreamInitalizer().initXStream(super.getJsonXStream());
  }

  protected LdapServer getLdapServer() {
    return ldapServer;
  }

  protected <T> T getFromResponse(Class<T> clazz, XStream xstream, Response response)
      throws IOException
  {
    String responseText = response.getEntity().getText();
    return this.getFromResponse(clazz, xstream, responseText);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getFromResponse(Class<T> clazz, XStream xstream, String responseText) {
    return (T) xstream.fromXML(responseText);
  }

  protected boolean isStartServer() {
    return true;
  }

  protected LdapServerRequest getDefaultServerRequest() {
    LdapServerRequest serverRequest = new LdapServerRequest();
    serverRequest.setData(getDefaultServerConfiguration());
    return serverRequest;
  }

  protected LdapServerConfigurationDTO getDefaultServerConfiguration() {
    LdapConnectionInfoDTO connInfo = getDefaultConnectionInfo();
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = getDefaultUserAndGroupConfiguration();

    LdapServerConfigurationDTO dto = new LdapServerConfigurationDTO();
    dto.setId(null); // not set
    dto.setName("default");
    dto.setUrl(null); // set on the return, not the request
    dto.setConnectionInfo(connInfo);
    dto.setUserAndGroupConfig(userGroupConf);
    return dto;
  }

  protected LdapConnectionInfoDTO getDefaultConnectionInfo() {
    LdapConnectionInfoDTO connInfo = new LdapConnectionInfoDTO();

    connInfo.setAuthScheme("simple");
    connInfo.setCacheTimeout(30);
    connInfo.setConnectionRetryDelay(0);
    connInfo.setConnectionTimeout(10);
    connInfo.setHost("localhost");

    // using fixed port if ldap server is not to be started to be able to plug external server instance for debugging
    connInfo.setPort(isStartServer() ? this.getLdapServer().getPort() : 10389);

    connInfo.setProtocol("ldap");
    // connInfo.setRealm( "" );
    connInfo.setSearchBase("o=sonatype");
    connInfo.setSystemPassword(encodeBase64("secret"));
    connInfo.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    return connInfo;
  }

  protected LdapUserAndGroupAuthConfigurationDTO getDefaultUserAndGroupConfiguration() {
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();

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
    userGroupConf.setLdapFilter(null);
    userGroupConf.setUserSubtree(true);
    return userGroupConf;
  }

  public static String encodeBase64(final String value) {
    try {
      return Base64.encodeToString(value.getBytes(PREFERRED_ENCODING));
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

}

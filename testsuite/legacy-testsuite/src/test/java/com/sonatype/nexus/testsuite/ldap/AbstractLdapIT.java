/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.sonatype.ldaptestsuite.LdapServer;
import org.sonatype.ldaptestsuite.LdapServerConfiguration;
import org.sonatype.ldaptestsuite.Partition;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.security.realms.ldap.api.dto.LdapConnectionInfoDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.LdapServerRequest;
import org.sonatype.security.realms.ldap.api.dto.LdapUserAndGroupAuthConfigurationDTO;
import org.sonatype.security.realms.ldap.api.dto.XStreamInitalizer;
import org.sonatype.security.realms.ldap.client.Configuration;
import org.sonatype.security.realms.ldap.client.Connection;
import org.sonatype.security.realms.ldap.client.Connection.Host;
import org.sonatype.security.realms.ldap.client.Connection.Protocol;
import org.sonatype.security.realms.ldap.client.LdapConfigurations;
import org.sonatype.security.realms.ldap.client.Mapping;

import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import org.apache.shiro.codec.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.restlet.data.Response;

import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;
import static org.sonatype.nexus.client.rest.BaseUrl.baseUrlFrom;

public abstract class AbstractLdapIT
    extends AbstractNexusIntegrationTest
{
  private LdapServer ldapServer;

  @BeforeClass
  public static void setSecurity() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Before
  public void beforeLdapTests() throws Exception {
    ldapServer = new LdapServer(ldapServerConfiguration());
    if (shouldStartLdapServer()) {
      ldapServer.start();
    }
    safeCreateLdapClientConfiguration();
  }

  /**
   * Override this method if you don't want to have LDAP server started by method {@link #beforeLdapTests()}, but you
   * want to manage it manually (or test don't need LDAP server at all to be running).
   */
  protected boolean shouldStartLdapServer() {
    return true;
  }

  protected LdapServerConfiguration ldapServerConfiguration() {
    return LdapServerConfiguration.builder()
        .withWorkingDirectory(util.createTempDir())
        .withPartitions(Partition.builder()
                .withNameAndSuffix("sonatype", "o=sonatype")
                .withRootEntryClasses("top", "organization")
                .withIndexedAttributes("objectClass", "o")
                .withLdifFile(util.resolveFile("src/test/it-resources/default-config/test.ldif"))
                .build()
        )
        .build();
  }

  protected void safeCreateLdapClientConfiguration() throws Exception {
    final List<Configuration> configurations = getLdapClient().get();
    for (Configuration configuration : configurations) {
      configuration.remove();
    }
    createLdapClientConfiguration();
  }

  protected void createLdapClientConfiguration() throws Exception {
    final Configuration configuration = getLdapClient().create();
    configuration.setName(getTestId());
    final Connection connection = configuration.getConnection();
    connection.setSearchBase("o=sonatype");
    connection.setSystemUsername("uid=admin,ou=system");
    connection.setSystemPassword("secret");
    connection.setAuthScheme("simple");
    if (shouldStartLdapServer()) {
      connection.setHost(new Host(Protocol.ldap, "localhost", ldapServer.getPort()));
    }
    else {
      // ldap not started yet, dummy port
      connection.setHost(new Host(Protocol.ldap, "localhost", 1234));
    }
    final Mapping mapping = configuration.getMapping();
    mapping.setEmailAddressAttribute("mail");
    mapping.setLdapGroupsAsRoles(true);
    mapping.setGroupBaseDn("ou=groups");
    mapping.setGroupIdAttribute("cn");
    mapping.setGroupMemberAttribute("uniqueMember");
    mapping.setGroupMemberFormat("uid=${username},ou=people,o=sonatype");
    mapping.setGroupObjectClass("groupOfUniqueNames");
    mapping.setUserPasswordAttribute("userPassword");
    mapping.setUserIdAttribute("uid");
    mapping.setUserObjectClass("inetOrgPerson");
    mapping.setUserBaseDn("ou=people");
    mapping.setUserRealNameAttribute("sn");
    configuration.save();
  }

  @After
  public void afterLdapTests()
      throws Exception
  {
    if (ldapServer != null && ldapServer.isStarted()) {
      ldapServer.stop();
    }
    ldapServer = null;
  }

  protected NexusClient getNexusClient()
      throws Exception
  {
    final NexusClientFactory nexusClientFactory = TestContainer
        .getInstance().getPlexusContainer().lookup(JerseyNexusClientFactory.class);
    return nexusClientFactory.createFor(
        baseUrlFrom(getBaseNexusUrl()),
        new UsernamePasswordAuthenticationInfo("admin", "admin123")
    );
  }

  protected LdapConfigurations getLdapClient()
      throws Exception
  {
    return getNexusClient().getSubsystem(LdapConfigurations.class);
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
    connInfo.setPort(shouldStartLdapServer() ? this.getLdapServer().getPort() : 10389);

    connInfo.setProtocol("ldap");
    // connInfo.setRealm( "" );
    connInfo.setSearchBase("o=sonatype");
    connInfo.setSystemPassword(encodeBase64("secret"));
    connInfo.setSystemUsername(encodeBase64("uid=admin,ou=system"));
    return connInfo;
  }

  protected LdapUserAndGroupAuthConfigurationDTO getDefaultUserAndGroupConfiguration() {
    LdapUserAndGroupAuthConfigurationDTO userGroupConf = new LdapUserAndGroupAuthConfigurationDTO();

    // userGroupConf.setUserMemberOfAttribute("businesscategory");
    userGroupConf.setGroupBaseDn("ou=groups");
    userGroupConf.setGroupIdAttribute("cn");
    userGroupConf.setGroupMemberAttribute("uniqueMember");
    userGroupConf.setGroupMemberFormat("uid=${username},ou=people,o=sonatype");
    userGroupConf.setGroupObjectClass("groupOfUniqueNames");
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

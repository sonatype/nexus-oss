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
package com.sonatype.nexus.testsuite.ldap.nxcm5055;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.sonatype.nexus.ssl.client.Certificates;
import com.sonatype.nexus.ssl.client.TrustStore;
import com.sonatype.nexus.testsuite.ldap.AbstractLdapIT;
import com.sonatype.security.ldap.api.dto.LdapAuthenticationTestRequest;
import com.sonatype.security.ldap.api.dto.LdapServerConfigurationDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListEntryDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestDTO;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestRequest;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.client.rest.jersey.JerseyNexusClientFactory;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;
import org.sonatype.security.ldap.realms.tools.LdapURL;
import org.sonatype.security.rest.model.PlexusUserListResourceResponse;
import org.sonatype.security.rest.model.PlexusUserResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResource;
import org.sonatype.security.rest.model.PlexusUserSearchCriteriaResourceRequest;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;

import static com.sonatype.nexus.ldap.model.LdapTrustStoreKey.ldapTrustStoreKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.sonatype.nexus.client.rest.BaseUrl.baseUrlFrom;

/**
 * ITs related to enhanced SSL support.
 *
 * @since 2.4.1
 */
public class NXCM5055NexusTrustStoreIT
    extends AbstractLdapIT
{

  /**
   * Verify that an SSL LDAP server with a self signed certificate can be accessed once the self signed certificate
   * is
   * trusted.
   */
  @Test
  public void accessUsersFromSelfSignedLdapServer()
      throws Exception
  {
    final NexusClient nexusClient = getNexusClient();

    final LdapServerConfigurationDTO ldapServerConfig = getLdapServerConfiguration();
    final URI uri = getLdapServerUri(ldapServerConfig);

    // disable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).disableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // there will be no user as backend will fail to connect to LDAP server since it has an untrusted certificate
    {
      final List<PlexusUserResource> ldapUsers = getLdapUsers();
      assertThat(ldapUsers, hasSize(0));
    }

    // enable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).enableFor(ldapTrustStoreKey(ldapServerConfig.getId()));
    // trust ldap server certificate
    nexusClient.getSubsystem(Certificates.class).get(uri.getHost(), uri.getPort(), uri.getScheme()).save();

    // there should be at least one user as now the certificate is trusted so ldap server can be accessed
    {
      final List<PlexusUserResource> ldapUsers = getLdapUsers();
      assertThat(ldapUsers, hasSize(greaterThan(0)));
    }
  }

  /**
   * Verify that connection test works to an LDAP server with a self signed certificate.
   */
  @Test
  public void testConnectionToSelfSignedLdapServer()
      throws Exception
  {
    final NexusClient nexusClient = getNexusClient();

    final LdapServerConfigurationDTO ldapServerConfig = getLdapServerConfiguration();
    final URI uri = getLdapServerUri(ldapServerConfig);

    // trust ldap server certificate
    nexusClient.getSubsystem(Certificates.class).get(uri.getHost(), uri.getPort(), uri.getScheme()).save();

    // disable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).disableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test connection without sending the ldap server id. This will result in not using Nexus SSL Trust Store which
    // should result in a failure
    try {
      testConnection(ldapServerConfig, false);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // test connection with sending the ldap server id. Nexus SSL Trust Store will not be used as is not enabled
    // which should result in a failure
    try {
      testConnection(ldapServerConfig, true);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // enable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).enableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test connection with sending the ldap server id. Nexus SSL Trust Store will be used as it is enabled
    // which should result in a success
    testConnection(ldapServerConfig, true);

    // there should be at least one user as now the certificate is trusted so ldap server can be accessed
    {
      final List<PlexusUserResource> ldapUsers = getLdapUsers();
      assertThat(ldapUsers, hasSize(greaterThan(0)));
    }
  }

  /**
   * Verify that user & group test works to an LDAP server with a self signed certificate.
   */
  @Test
  public void testUserAndGroupMappingsToSelfSignedLdapServer()
      throws Exception
  {
    final NexusClient nexusClient = getNexusClient();

    final LdapServerConfigurationDTO ldapServerConfig = getLdapServerConfiguration();
    final URI uri = getLdapServerUri(ldapServerConfig);

    // trust ldap server certificate
    nexusClient.getSubsystem(Certificates.class).get(uri.getHost(), uri.getPort(), uri.getScheme()).save();

    // disable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).disableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test without sending the ldap server id. This will result in not using Nexus SSL Trust Store which
    // should result in a failure
    try {
      testUserAndGroupMappings(ldapServerConfig, false);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // test with sending the ldap server id. Nexus SSL Trust Store will not be used as is not enabled
    // which should result in a failure
    try {
      testUserAndGroupMappings(ldapServerConfig, true);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // enable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).enableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test with sending the ldap server id. Nexus SSL Trust Store will be used as it is enabled
    // which should result in a success
    testUserAndGroupMappings(ldapServerConfig, true);

    // there should be at least one user as now the certificate is trusted so ldap server can be accessed
    {
      final List<PlexusUserResource> ldapUsers = getLdapUsers();
      assertThat(ldapUsers, hasSize(greaterThan(0)));
    }
  }

  /**
   * Verify that login test works to an LDAP server with a self signed certificate.
   */
  @Test
  public void testLoginToSelfSignedLdapServer()
      throws Exception
  {
    final NexusClient nexusClient = getNexusClient();

    final LdapServerConfigurationDTO ldapServerConfig = getLdapServerConfiguration();
    final URI uri = getLdapServerUri(ldapServerConfig);

    // trust ldap server certificate
    nexusClient.getSubsystem(Certificates.class).get(uri.getHost(), uri.getPort(), uri.getScheme()).save();

    // disable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).disableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test without sending the ldap server id. This will result in not using Nexus SSL Trust Store which
    // should result in a failure
    try {
      testLogin(ldapServerConfig, false);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // test with sending the ldap server id. Nexus SSL Trust Store will not be used as is not enabled
    // which should result in a failure
    try {
      testLogin(ldapServerConfig, true);
      assertThat("Expected to fail with Exception", false);
    }
    catch (Exception e) {
      assertThat(e.getMessage(), containsString("unable to find valid certification path"));
    }

    // enable Nexus TrustStore for default server
    nexusClient.getSubsystem(TrustStore.class).enableFor(ldapTrustStoreKey(ldapServerConfig.getId()));

    // test with sending the ldap server id. Nexus SSL Trust Store will be used as it is enabled
    // which should result in a success
    testLogin(ldapServerConfig, true);

    // there should be at least one user as now the certificate is trusted so ldap server can be accessed
    {
      final List<PlexusUserResource> ldapUsers = getLdapUsers();
      assertThat(ldapUsers, hasSize(greaterThan(0)));
    }
  }

  private void testLogin(final LdapServerConfigurationDTO ldapServer, final boolean sendServerId)
      throws Exception
  {
    final LdapServerLoginTestDTO loginDTO = new LdapServerLoginTestDTO();
    loginDTO.setUsername(encodeBase64("brianf"));
    loginDTO.setPassword(encodeBase64("brianf123"));
    loginDTO.setConfiguration(ldapServer);

    ldapServer.setUrl(null);

    final LdapServerLoginTestRequest resourceRequest = new LdapServerLoginTestRequest();
    resourceRequest.setData(loginDTO);

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/logintest"
              + (sendServerId ? "?ldapServerId=" + ldapServer.getId() : ""),
          Method.PUT,
          new XStreamRepresentation(xstream, xstream.toXML(resourceRequest), MediaType.APPLICATION_XML));

      if (response.getStatus().getCode() == 400) {
        final ErrorResponse errorResponse = this.getFromResponse(ErrorResponse.class, xstream, response);
        assertThat(errorResponse, is(notNullValue()));

        // FIXME: This is causing compilation exceptions post-merge
        //assertThat( errorResponse.getErrors(), hasSize( greaterThan( 0 ) ) );

        throw new Exception(((ErrorMessage) errorResponse.getErrors().get(0)).getMsg());
      }

      assertThat(response.getStatus().getCode(), is(204));
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private void testUserAndGroupMappings(final LdapServerConfigurationDTO ldapServer, final boolean sendServerId)
      throws Exception
  {
    final LdapServerRequest resourceRequest = new LdapServerRequest();
    resourceRequest.setData(ldapServer);
    resourceRequest.getData().setUrl(null);

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/test_user_conf"
              + (sendServerId ? "?ldapServerId=" + ldapServer.getId() : ""),
          Method.PUT,
          new XStreamRepresentation(xstream, xstream.toXML(resourceRequest), MediaType.APPLICATION_XML));

      if (response.getStatus().getCode() == 400) {
        final ErrorResponse errorResponse = this.getFromResponse(ErrorResponse.class, xstream, response);
        assertThat(errorResponse, is(notNullValue()));

        // FIXME: This is causing compilation exceptions post-merge
        //assertThat( errorResponse.getErrors(), hasSize( greaterThan( 0 ) ) );

        throw new Exception(((ErrorMessage) errorResponse.getErrors().get(0)).getMsg());
      }

      assertThat(response.getStatus().getCode(), is(200));
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private void testConnection(final LdapServerConfigurationDTO ldapServer, final boolean sendServerId)
      throws Exception
  {
    final LdapAuthenticationTestRequest resourceRequest = new LdapAuthenticationTestRequest();
    resourceRequest.setData(ldapServer.getConnectionInfo());

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "ldap/test_auth"
              + (sendServerId ? "?ldapServerId=" + ldapServer.getId() : ""),
          Method.PUT,
          new XStreamRepresentation(xstream, xstream.toXML(resourceRequest), MediaType.APPLICATION_XML));

      if (response.getStatus().getCode() == 400) {
        final ErrorResponse errorResponse = this.getFromResponse(ErrorResponse.class, xstream, response);
        assertThat(errorResponse, is(notNullValue()));

        // FIXME: This is causing compilation exceptions post-merge
        //assertThat( errorResponse.getErrors(), hasSize( greaterThan( 0 ) ) );

        throw new Exception(((ErrorMessage) errorResponse.getErrors().get(0)).getMsg());
      }

      assertThat(response.getStatus().getCode(), is(204));
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private URI getLdapServerUri(final LdapServerConfigurationDTO ldapServer)
      throws Exception
  {
    // which is an LDAPS server
    final URI uri = new URI(new LdapURL(
        ldapServer.getConnectionInfo().getProtocol(),
        ldapServer.getConnectionInfo().getHost(),
        ldapServer.getConnectionInfo().getPort(),
        ldapServer.getConnectionInfo().getSearchBase()).toString()
    );

    assertThat(uri.getScheme(), is("ldaps"));
    return uri;
  }

  private LdapServerConfigurationDTO getLdapServerConfiguration()
      throws Exception
  {
    // there should be at least one ldap server
    final List<LdapServerListEntryDTO> ldapServers = getLdapServers();
    assertThat(ldapServers, hasSize(greaterThan(0)));
    return getLdapServer(ldapServers.get(0).getId());
  }

  private LdapServerConfigurationDTO getLdapServer(final String ldapServerId)
      throws Exception
  {
    Response response = null;
    try {
      response = RequestFacade.doGetRequest(RequestFacade.SERVICE_LOCAL + "ldap/servers/" + ldapServerId);
      LdapServerRequest ldapServerRequest = this.getFromResponse(
          LdapServerRequest.class,
          this.getXMLXStream(),
          response);

      return ldapServerRequest.getData();

    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private List<PlexusUserResource> getLdapUsers()
      throws IOException
  {
    PlexusUserSearchCriteriaResourceRequest resourceRequest = new PlexusUserSearchCriteriaResourceRequest();
    PlexusUserSearchCriteriaResource criteria = new PlexusUserSearchCriteriaResource();
    resourceRequest.setData(criteria);

    XStream xstream = this.getXMLXStream();
    Response response = null;
    try {
      response = RequestFacade.sendMessage(
          RequestFacade.SERVICE_LOCAL + "user_search/LDAP",
          Method.PUT,
          new XStreamRepresentation(xstream, xstream.toXML(resourceRequest), MediaType.APPLICATION_XML));

      assertThat(response.getStatus().getCode(), is(200));

      final PlexusUserListResourceResponse userList = this.getFromResponse(
          PlexusUserListResourceResponse.class, xstream, response
      );

      assertThat(userList, is(notNullValue()));
      assertThat(userList.getData(), is(notNullValue()));

      return userList.getData();
    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  public List<LdapServerListEntryDTO> getLdapServers()
      throws Exception
  {
    Response response = null;
    try {
      response = RequestFacade.doGetRequest(RequestFacade.SERVICE_LOCAL + "ldap/servers");
      LdapServerListResponse listResponse = this.getFromResponse(
          LdapServerListResponse.class,
          this.getXMLXStream(),
          response);

      return listResponse.getData();

    }
    finally {
      RequestFacade.releaseResponse(response);
    }
  }

  private NexusClient getNexusClient()
      throws Exception
  {
    final NexusClientFactory nexusClientFactory = TestContainer
        .getInstance().getPlexusContainer().lookup(JerseyNexusClientFactory.class);
    return nexusClientFactory.createFor(
        baseUrlFrom(getBaseNexusUrl()),
        new UsernamePasswordAuthenticationInfo("admin", "admin123")
    );
  }

}

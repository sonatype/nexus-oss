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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.EnterpriseLdapManager;
import com.sonatype.security.ldap.api.dto.LdapServerLoginTestRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

import org.apache.shiro.codec.Base64;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resource used to test the LDAP server configuration and user login.
 */
@Path("/ldap/logintest")
@Consumes({"application/xml", "application/json"})
@Named("LdapServerLoginTestPlexusResource")
@Singleton
public class LdapServerLoginTestPlexusResource
    extends AbstractLdapPlexusResource
{

  private final EnterpriseLdapManager ldapManager;

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapServerLoginTestPlexusResource(final TrustStore trustStore,
                                           final EnterpriseLdapManager ldapManager,
                                           final LdapConfigurationManager ldapConfigurationManager)
  {
    super(trustStore);
    this.ldapManager = checkNotNull(ldapManager);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.setReadable(false);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new LdapServerLoginTestRequest();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/logintest";
  }

  /**
   * Test login using a ldap configuration.
   */
  @Override
  @PUT
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    return super.put(context, request, response, payload);
  }

  protected Object doPut(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
  {
    if (payload == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing payload");
    }

    LdapServerLoginTestRequest ldapServerLoginTestRequest = (LdapServerLoginTestRequest) payload;

    if (ldapServerLoginTestRequest.getData() == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Payload is empty");
    }

    if (ldapServerLoginTestRequest.getData().getConfiguration() == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Payload configuration is empty");
    }

    // validate
    String username = Base64.decodeToString(ldapServerLoginTestRequest.getData().getUsername());
    if (StringUtils.isEmpty(username)) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing username argument.");
    }

    String password = Base64.decodeToString(ldapServerLoginTestRequest.getData().getPassword());
    if (StringUtils.isEmpty(password)) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing password argument.");
    }

    // get the ldap model object
    CLdapServerConfiguration ldapServer = toLdapModel(ldapServerLoginTestRequest.getData().getConfiguration());
    if (ldapServer.getId() == null && request.getResourceRef() != null) {
      ldapServer.setId(request.getResourceRef().getQueryAsForm().getFirstValue("ldapServerId"));
    }
    replaceFakePassword(ldapServer.getConnectionInfo(), ldapServer.getId(), ldapConfigurationManager);

    // try the login
    try {
      this.ldapManager.authenticateUserTest(username, password, ldapServer);
    }
    catch (Exception e) {
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Login attempt failed: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("Error connecting to LDAP Server: ", e)));
    }

    // success
    response.setStatus(Status.SUCCESS_NO_CONTENT);
    return null;
  }

}

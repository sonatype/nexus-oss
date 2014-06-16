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

import java.net.MalformedURLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.api.dto.LdapAuthenticationTestRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.validation.LdapConfigurationValidator;
import com.sonatype.security.ldap.realms.persist.model.CConnectionInfo;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.ldap.dao.LdapConnectionTester;

import org.apache.shiro.realm.ldap.LdapContextFactory;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resource used to test the LDAP server authentication.
 */
@Path("/ldap/test_auth")
@Consumes({"application/xml", "application/json"})
@Named("LdapTestAuthenticationPlexusResource")
@Singleton
public class LdapTestAuthenticationPlexusResource
    extends AbstractLdapPlexusResource
{

  private final LdapConfigurationValidator configurationValidator;

  private final LdapConnectionTester ldapConnectionTester;

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapTestAuthenticationPlexusResource(final TrustStore trustStore,
                                              final LdapConfigurationValidator configurationValidator,
                                              final LdapConnectionTester ldapConnectionTester,
                                              final LdapConfigurationManager ldapConfigurationManager)
  {
    super(trustStore);
    this.configurationValidator = checkNotNull(configurationValidator);
    this.ldapConnectionTester = checkNotNull(ldapConnectionTester);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new LdapAuthenticationTestRequest();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/test_auth";
  }

  /**
   * Test ldap server authentication.
   */
  @Override
  @PUT
  public Object put(Context context, Request request, Response response, Object payload)
      throws ResourceException
  {
    return super.put(context, request, response, payload);
  }

  @Override
  protected Object doPut(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
  {

    LdapAuthenticationTestRequest authRequest = (LdapAuthenticationTestRequest) payload;

    CConnectionInfo connectionInfo = this.toLdapModel(authRequest.getData());
    String ldapServerId = null;
    if (request.getResourceRef() != null) {
      ldapServerId = request.getResourceRef().getQueryAsForm().getFirstValue("ldapServerId");
    }
    replaceFakePassword(connectionInfo, ldapServerId, ldapConfigurationManager);

    ValidationResponse validationResponse = this.configurationValidator.validateConnectionInfo(
        null,
        connectionInfo);
    // if the info is not valid throw
    if (!validationResponse.isValid()) {
      throw new InvalidConfigurationException(validationResponse);
    }

    try {
      LdapContextFactory ldapContextFactory = this.buildDefaultLdapContextFactory(ldapServerId, connectionInfo);
      this.ldapConnectionTester.testConnection(ldapContextFactory);
    }
    catch (MalformedURLException e) {
      // should NEVER hit this
      this.getLogger().debug("LDAP Realm is not configured correctly: " + e.getMessage(), e);
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Realm is not configured correctly: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("LDAP Server Connection information is invalid: ", e)));
    }
    catch (Exception e) {
      this.getLogger().debug("Failed to connect to Ldap Server.", e);
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Realm is not configured correctly: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("Failed to connect to Ldap Server: ", e)));
    }

    response.setStatus(Status.SUCCESS_NO_CONTENT);
    return null;
  }
}

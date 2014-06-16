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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Manage an existing ldap server, GET, PUT, DELETE available.
 */
@Path("/ldap/servers/{" + LdapServerPlexusResource.LDAP_SERVER_ID + "}")
@Consumes({"application/xml", "application/json"})
@Produces({"application/xml", "application/json"})
@Named("LdapServerPlexusResource")
@Singleton
public class LdapServerPlexusResource
    extends AbstractLdapPlexusResource
{

  public static final String LDAP_SERVER_ID = "serverId";

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapServerPlexusResource(final TrustStore trustStore,
                                  final LdapConfigurationManager ldapConfigurationManager)
  {
    super(trustStore);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new LdapServerRequest();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/ldap/servers/*", "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/servers/{" + LDAP_SERVER_ID + "}";
  }

  private String getServerId(Request request) {
    return (String) request.getAttributes().get(LDAP_SERVER_ID);
  }

  /**
   * Get the configuration of an ldap server.
   */
  @Override
  @GET
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    return super.get(context, request, response, variant);
  }

  @Override
  protected Object doGet(Context context, Request request, Response response, Variant variant)
      throws ResourceException, InvalidConfigurationException
  {
    try {
      String serverId = this.getServerId(request);
      return this.getLdapServerRequest(request, serverId);
    }
    catch (LdapServerNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
    }
  }

  private LdapServerRequest getLdapServerRequest(Request request, String serverId)
      throws InvalidConfigurationException, LdapServerNotFoundException
  {

    LdapServerRequest ldapRequest = new LdapServerRequest();
    ldapRequest.setData(this.toDto(this.ldapConfigurationManager.getLdapServerConfiguration(serverId)));
    // a hack, as we lack API to create reference to "this", only create "child" ref
    final String refUrl = createChildReference(request, "foo").toString();
    ldapRequest.getData().setUrl(refUrl.substring(0, refUrl.length() - 4));

    return ldapRequest;

  }

  /**
   * Update an existing ldap configuration.
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
      throws ResourceException, InvalidConfigurationException
  {
    LdapServerRequest ldapServerRequest = (LdapServerRequest) payload;
    if (payload == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing payload");
    }

    if (ldapServerRequest.getData() == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Payload is empty");
    }

    String serverId = this.getServerId(request);

    CLdapServerConfiguration ldapServer = this.toLdapModel(ldapServerRequest.getData());
    ldapServer.setId(serverId);
    replaceFakePassword(ldapServer.getConnectionInfo(), ldapServer.getId(), ldapConfigurationManager);

    try {
      this.ldapConfigurationManager.updateLdapServerConfiguration(ldapServer);
    }
    catch (LdapServerNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
    }

    // return the new object
    try {
      return this.getLdapServerRequest(request, serverId);
    }
    catch (LdapServerNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
          "Repository was created successfully, but could not be retrieved.", e);
    }
  }

  /**
   * Delete an existing ldap server.
   */
  @Override
  @DELETE
  public void delete(Context context, Request request, Response response)
      throws ResourceException
  {
    super.delete(context, request, response);
  }

  @Override
  protected void doDelete(Context context, Request request, Response response)
      throws ResourceException, InvalidConfigurationException
  {
    String serverId = this.getServerId(request);
    try {
      this.ldapConfigurationManager.deleteLdapServerConfiguration(serverId);
    }
    catch (LdapServerNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e);
    }
  }

}

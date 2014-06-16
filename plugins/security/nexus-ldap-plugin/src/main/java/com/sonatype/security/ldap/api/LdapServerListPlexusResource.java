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
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.api.dto.LdapServerListEntryDTO;
import com.sonatype.security.ldap.api.dto.LdapServerListResponse;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.LdapServerNotFoundException;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.security.ldap.realms.tools.LdapURL;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/ldap/servers")
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
@Named("LdapServerListPlexusResource")
@Singleton
public class LdapServerListPlexusResource
    extends AbstractLdapPlexusResource
{

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapServerListPlexusResource(final TrustStore trustStore,
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
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/servers";
  }

  /**
   * Get the list of ldap servers configured in nexus (ordered by relevance).
   */
  @Override
  @GET
  public LdapServerListResponse get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {

    LdapServerListResponse listResponse = new LdapServerListResponse();
    List<LdapServerListEntryDTO> entries = new ArrayList<LdapServerListEntryDTO>();
    for (CLdapServerConfiguration ldapServer : this.ldapConfigurationManager.listLdapServerConfigurations()) {
      entries.add(this.toListEntry(request, ldapServer));
    }

    listResponse.setData(entries);
    return listResponse;
  }

  /**
   * Add a new ldap server to nexus.
   */
  @Override
  @POST
  public LdapServerRequest doPost(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
  {
    LdapServerRequest ldapServerRequest = (LdapServerRequest) payload;
    if (payload == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing payload");
    }

    if (ldapServerRequest.getData() == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Payload is empty");
    }

    CLdapServerConfiguration ldapServer = this.toLdapModel(ldapServerRequest.getData());
    this.ldapConfigurationManager.addLdapServerConfiguration(ldapServer);

    try {
      LdapServerRequest ldapServerResponse = new LdapServerRequest();
      ldapServerResponse
          .setData(this.toDto(this.ldapConfigurationManager.getLdapServerConfiguration(ldapServer.getId())));
      // need to update the url with the new url
      ldapServerResponse.getData().setUrl(this.createChildReference(request, ldapServer.getId()).toString());

      return ldapServerResponse;
    }
    catch (LdapServerNotFoundException e) {
      throw new ResourceException(
          Status.SERVER_ERROR_INTERNAL,
          "Repository was created successfully, but could not be retrieved.",
          e);
    }
  }

  private LdapServerListEntryDTO toListEntry(Request request, CLdapServerConfiguration ldapServer) {
    LdapServerListEntryDTO listEntry = new LdapServerListEntryDTO();
    listEntry.setId(ldapServer.getId());
    listEntry.setName(ldapServer.getName());
    listEntry.setUrl(this.createChildReference(request, ldapServer.getId()).toString());

    try {
      listEntry.setLdapUrl(new LdapURL(ldapServer.getConnectionInfo().getProtocol(), ldapServer
          .getConnectionInfo().getHost(), ldapServer.getConnectionInfo().getPort(), ldapServer
          .getConnectionInfo().getSearchBase()).toString());
    }
    catch (MalformedURLException e) {
      this.getLogger().warn("Ldap Server has invalid URL", e);
      listEntry.setLdapUrl("FIX: Invalid LDAP URL");
    }

    return listEntry;
  }
}

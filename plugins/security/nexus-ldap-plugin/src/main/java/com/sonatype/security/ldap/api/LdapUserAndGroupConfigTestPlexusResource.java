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
import java.util.Set;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.sonatype.nexus.ssl.plugin.TrustStore;
import com.sonatype.security.ldap.LdapConnectionUtils;
import com.sonatype.security.ldap.api.dto.LdapServerRequest;
import com.sonatype.security.ldap.api.dto.LdapUserDTO;
import com.sonatype.security.ldap.api.dto.LdapUserListResponse;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;
import com.sonatype.security.ldap.persist.validation.LdapConfigurationValidator;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.security.ldap.dao.LdapConnectionTester;
import org.sonatype.security.ldap.dao.LdapDAOException;
import org.sonatype.security.ldap.dao.LdapUser;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/ldap/test_user_conf")
@Consumes({"application/xml", "application/json"})
@Named("LdapUserAndGroupConfigTestPlexusResource")
@Singleton
public class LdapUserAndGroupConfigTestPlexusResource
    extends AbstractLdapPlexusResource
{

  private final LdapConfigurationValidator configurationValidator;

  private final LdapConnectionTester ldapConnectionTester;

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapUserAndGroupConfigTestPlexusResource(final TrustStore trustStore,
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
    return new LdapServerRequest();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/test_user_conf";
  }

  /**
   * Test ldap server user and group configuration.
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
    LdapServerRequest ldapServerRequest = (LdapServerRequest) payload;
    if (payload == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Missing payload");
    }

    if (ldapServerRequest.getData() == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Payload is empty");
    }

    // get the ldap model object
    CLdapServerConfiguration ldapServer = toLdapModel(ldapServerRequest.getData());
    if (ldapServer.getId() == null && request.getResourceRef() != null) {
      ldapServer.setId(request.getResourceRef().getQueryAsForm().getFirstValue("ldapServerId"));
    }
    replaceFakePassword(ldapServer.getConnectionInfo(), ldapServer.getId(), ldapConfigurationManager);

    // validate it
    ValidationResponse validationResponse = this.configurationValidator.validateLdapServerConfiguration(
        ldapServer,
        false);
    // if the info is not valid throw
    if (!validationResponse.isValid()) {
      throw new InvalidConfigurationException(validationResponse);
    }

    // its valid, now we need to deal with getting the users
    LdapUserListResponse result = new LdapUserListResponse();
    try {
      // get the users
      SortedSet<LdapUser> ldapUsers = ldapConnectionTester.testUserAndGroupMapping(
          buildDefaultLdapContextFactory(ldapServer.getId(), ldapServer.getConnectionInfo()),
          LdapConnectionUtils.getLdapAuthConfiguration(ldapServer),
          20
      );

      // now add them to the result
      for (LdapUser ldapUser : ldapUsers) {
        result.getData().add(this.toDto(ldapUser));
      }

    }
    catch (MalformedURLException e) {
      getLogger().debug("LDAP Realm is not configured correctly: " + e.getMessage(), e);
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Realm is not configured correctly: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("LDAP Server Connection information is invalid: ", e)));
    }
    catch (LdapDAOException e) {
      getLogger().debug("LDAP Realm is not configured correctly: " + e.getMessage(), e);
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Realm is not configured correctly: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("Invalid LDAP Server Configuration: ", e)));
    }
    catch (NamingException e) {
      getLogger().debug("LDAP Realm is not configured correctly: " + e.getMessage(), e);
      throw new PlexusResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "LDAP Realm is not configured correctly: "
          + e.getMessage(), e,
          this.getErrorResponse("*", this.buildExceptionMessage("Error connecting to LDAP Server: ", e)));
    }
    return result;
  }

  private LdapUserDTO toDto(LdapUser ldapUser) {
    LdapUserDTO dto;

    dto = new LdapUserDTO();

    // now set the rest of the props
    dto.setUserId(ldapUser.getUsername());
    dto.setEmail(ldapUser.getEmail());
    dto.setName(ldapUser.getRealName());

    // add the roles
    for (String role : (Set<String>) ldapUser.getMembership()) {
      dto.addRole(role);
    }
    return dto;
  }

}

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
import com.sonatype.security.ldap.api.dto.LdapServerOrderRequest;
import com.sonatype.security.ldap.persist.LdapConfigurationManager;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Resource used to assign the order of ldap servers in nexus.  The servers will be accessed in this order.
 */
@Path("/ldap/server_order")
@Consumes({"application/xml", "application/json"})
@Named("LdapServerOrderPlexusResource")
@Singleton
public class LdapServerOrderPlexusResource
    extends AbstractLdapPlexusResource
{

  private final LdapConfigurationManager ldapConfigurationManager;

  @Inject
  public LdapServerOrderPlexusResource(final TrustStore trustStore,
                                       final LdapConfigurationManager ldapConfigurationManager)
  {
    super(trustStore);
    this.ldapConfigurationManager = checkNotNull(ldapConfigurationManager);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new LdapServerOrderRequest();
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:ldapconfig]");
  }

  @Override
  public String getResourceUri() {
    return "/ldap/server_order";
  }

  /**
   * Set the access order of ldap servers in nexus.
   */
  @Override
  @PUT
  public LdapServerOrderRequest doPut(Context context, Request request, Response response, Object payload)
      throws ResourceException,
             InvalidConfigurationException
  {
    LdapServerOrderRequest dto = (LdapServerOrderRequest) payload;
    this.ldapConfigurationManager.setServerOrder(dto.getData());
    return dto;
  }

}

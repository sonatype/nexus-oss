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
package org.sonatype.security.rest.privileges;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.realms.privileges.PrivilegeDescriptor;
import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;
import org.sonatype.security.rest.model.PrivilegeTypePropertyResource;
import org.sonatype.security.rest.model.PrivilegeTypeResource;
import org.sonatype.security.rest.model.PrivilegeTypeResourceResponse;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * REST resource to retrieve the list of Privilege Types. Each type of privilege that can be created is described by a
 * {@link PrivilegeTypeResource}. Each PrivilegeTypeResource lists the set of properties used to define a type of
 * privilege.
 *
 * @author bdemers
 */
@Singleton
@Typed(PlexusResource.class)
@Named("PrivilegeTypePlexusResource")
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
@Path(PrivilegeTypePlexusResource.RESOURCE_URI)
public class PrivilegeTypePlexusResource
    extends AbstractPrivilegePlexusResource
{

  public static final String RESOURCE_URI = "/privilege_types";

  private final List<PrivilegeDescriptor> privilegeDescriptors;

  @Inject
  public PrivilegeTypePlexusResource(final List<PrivilegeDescriptor> privilegeDescriptors) {
    this.privilegeDescriptors = checkNotNull(privilegeDescriptors);
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[security:privilegetypes]");
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  /**
   * Retrieves the list of privilege types.
   */
  @Override
  @GET
  public PrivilegeTypeResourceResponse get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    PrivilegeTypeResourceResponse result = new PrivilegeTypeResourceResponse();

    for (PrivilegeDescriptor descriptor : privilegeDescriptors) {
      PrivilegeTypeResource type = new PrivilegeTypeResource();
      type.setId(descriptor.getType());
      type.setName(descriptor.getName());

      for (PrivilegePropertyDescriptor property : descriptor.getPropertyDescriptors()) {
        PrivilegeTypePropertyResource typeProp = new PrivilegeTypePropertyResource();
        typeProp.setId(property.getId());
        typeProp.setName(property.getName());
        typeProp.setHelpText(property.getHelpText());
        typeProp.setType(property.getType());
        type.addProperty(typeProp);
      }

      result.addData(type);
    }

    return result;
  }
}

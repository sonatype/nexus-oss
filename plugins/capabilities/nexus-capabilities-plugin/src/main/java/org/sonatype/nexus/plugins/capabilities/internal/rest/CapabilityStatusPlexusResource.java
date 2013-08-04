/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.plugins.capabilities.internal.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityNotFoundException;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityStatusResponseResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.getCapabilityIdentity;

@Named
@Singleton
@Path(CapabilityStatusPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class CapabilityStatusPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

  public static final String CAPABILITIES_ID_KEY = "capabilityId";

  public static final String RESOURCE_URI = "/capabilities/{" + CAPABILITIES_ID_KEY + "}/status";

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public CapabilityStatusPlexusResource(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new CapabilityStatusRequestResource();
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/capabilities/*/status", "authcBasic,perms[nexus:capabilities]");
  }

  @Override
  @GET
  @ResourceMethodSignature(
      pathParams = {
          @PathParam(CAPABILITIES_ID_KEY)
      },
      output = CapabilityStatusResponseResource.class
  )
  public Object get(final Context context, final Request request, final Response response, final Variant variant)
      throws ResourceException
  {
    final CapabilityIdentity capabilityId = getCapabilityIdentity(request);
    final CapabilityReference reference = capabilityRegistry.get(capabilityId);
    if (reference == null) {
      throw new ResourceException(
          Status.CLIENT_ERROR_NOT_FOUND,
          String.format("Cannot find a capability with specified id of %s", capabilityId)
      );
    }

    return asCapabilityStatusResponseResource(
        reference,
        request.getResourceRef().toString()
    );
  }

  /**
   * Update the configuration of an existing capability.
   */
  @Override
  @PUT
  @ResourceMethodSignature(
      pathParams = {
          @PathParam(CAPABILITIES_ID_KEY)
      },
      input = CapabilityStatusRequestResource.class,
      output = CapabilityStatusResponseResource.class
  )
  public Object put(final Context context, final Request request, final Response response, final Object payload)
      throws ResourceException
  {
    final CapabilityIdentity capabilityId = getCapabilityIdentity(request);
    final CapabilityStatusRequestResource envelope = (CapabilityStatusRequestResource) payload;
    try {
      CapabilityReference reference;

      try {
        if (envelope.getData().isEnabled()) {
          reference = capabilityRegistry.enable(capabilityId);
        }
        else {
          reference = capabilityRegistry.disable(capabilityId);
        }
      }
      catch (CapabilityNotFoundException e) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
      }

      return asCapabilityStatusResponseResource(
          reference,
          request.getResourceRef().toString()
      );
    }
    catch (final IOException e) {
      throw new ResourceException(
          Status.SERVER_ERROR_INTERNAL, "Could not manage capabilities configuration persistence store"
      );
    }
  }

}

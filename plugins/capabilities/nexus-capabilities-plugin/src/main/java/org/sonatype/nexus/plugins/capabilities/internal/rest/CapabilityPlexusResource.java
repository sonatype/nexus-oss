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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityNotFoundException;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.google.common.collect.Maps;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;

@Named
@Singleton
@Path(CapabilityPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class CapabilityPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

  public static final String CAPABILITIES_ID_KEY = "capabilityId";

  public static final String RESOURCE_URI = "/capabilities/{" + CAPABILITIES_ID_KEY + "}";

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public CapabilityPlexusResource(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
    this.setModifiable(true);
  }

  @Override
  public Object getPayloadInstance() {
    return new CapabilityRequestResource();
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor("/capabilities/*", "authcBasic,perms[nexus:capabilities]");
  }

  /**
   * Get the details of a capability.
   */
  @Override
  @GET
  @ResourceMethodSignature(
      pathParams = {
          @PathParam(CAPABILITIES_ID_KEY)
      },
      output = CapabilityResponseResource.class
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

    return asCapabilityResponseResource(reference);
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
      input = CapabilityRequestResource.class,
      output = CapabilityStatusResponseResource.class
  )
  public Object put(final Context context, final Request request, final Response response, final Object payload)
      throws ResourceException
  {
    final CapabilityIdentity capabilityId = getCapabilityIdentity(request);
    final CapabilityRequestResource envelope = (CapabilityRequestResource) payload;
    try {
      final CapabilityReference reference = capabilityRegistry.update(
          capabilityId,
          envelope.getData().isEnabled(),
          envelope.getData().getNotes(),
          asMap(envelope.getData().getProperties())
      );
      return asCapabilityStatusResponseResource(
          reference,
          createChildReference(request, this, capabilityId.toString()).toString()
      );
    }
    catch (CapabilityNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
    }
    catch (final InvalidConfigurationException e) {
      handleConfigurationException(e);
      return null;
    }
    catch (final IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
          "Could not manage capabilities configuration persistence store");
    }
  }

  /**
   * Delete an existing capability.
   */
  @Override
  @DELETE
  @ResourceMethodSignature(
      pathParams = {
          @PathParam(CAPABILITIES_ID_KEY)
      }
  )
  public void delete(final Context context, final Request request, final Response response)
      throws ResourceException
  {
    try {
      capabilityRegistry.remove(getCapabilityIdentity(request));
      response.setStatus(Status.SUCCESS_NO_CONTENT);
    }
    catch (CapabilityNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, e.getMessage());
    }
    catch (final IOException e) {
      throw new ResourceException(
          Status.SERVER_ERROR_INTERNAL, "Could not manage capabilities configuration persistence store"
      );
    }
  }

  static Map<String, String> asMap(final List<CapabilityPropertyResource> properties) {
    final Map<String, String> map = Maps.newHashMap();

    if (properties != null) {
      for (final CapabilityPropertyResource property : properties) {
        map.put(property.getKey(), property.getValue());
      }
    }

    return map;
  }

  static CapabilityResponseResource asCapabilityResponseResource(final CapabilityReference reference) {
    checkNotNull(reference);

    final CapabilityResource resource = new CapabilityResource();

    resource.setId(reference.context().id().toString());
    resource.setNotes(reference.context().notes());
    resource.setEnabled(reference.context().isEnabled());
    resource.setTypeId(reference.context().type().toString());

    if (reference.context().properties() != null) {
      for (final Map.Entry<String, String> entry : reference.context().properties().entrySet()) {
        final CapabilityPropertyResource resourceProp = new CapabilityPropertyResource();
        resourceProp.setKey(entry.getKey());
        resourceProp.setValue(entry.getValue());

        resource.addProperty(resourceProp);
      }
    }

    final CapabilityResponseResource response = new CapabilityResponseResource();
    response.setData(resource);

    return response;
  }

  static CapabilityStatusResponseResource asCapabilityStatusResponseResource(final CapabilityReference reference,
                                                                             final String uri)
  {
    checkNotNull(reference);

    final CapabilityStatusResponseResource status = new CapabilityStatusResponseResource();

    status.setData(asCapabilityListItemResource(reference, uri));

    return status;
  }

  static CapabilityListItemResource asCapabilityListItemResource(final CapabilityReference reference,
                                                                 final String uri)
  {
    checkNotNull(reference);

    final CapabilityListItemResource item = new CapabilityListItemResource();
    item.setId(reference.context().id().toString());
    item.setNotes(reference.context().notes());
    item.setEnabled(reference.context().isEnabled());
    item.setTypeId(reference.context().type().toString());
    item.setTypeName(reference.context().descriptor().name());
    item.setActive(reference.context().isActive());
    item.setError(reference.context().hasFailure());
    try {
      item.setDescription(reference.capability().description());
    }
    catch (Exception ignore) {
      item.setDescription(null);
    }
    try {
      item.setStatus(reference.capability().status());
    }
    catch (Exception ignore) {
      item.setStatus(null);
    }
    item.setStateDescription(reference.context().stateDescription());

    if (reference.context().properties() != null) {
      for (final Map.Entry<String, String> entry : reference.context().properties().entrySet()) {
        final CapabilityPropertyResource resourceProp = new CapabilityPropertyResource();
        resourceProp.setKey(entry.getKey());
        resourceProp.setValue(entry.getValue());

        item.addProperty(resourceProp);
      }
    }

    item.setResourceURI(uri);

    return item;
  }

  static CapabilityIdentity getCapabilityIdentity(final Request request) {
    return capabilityIdentity(request.getAttributes().get(CAPABILITIES_ID_KEY).toString());
  }

}

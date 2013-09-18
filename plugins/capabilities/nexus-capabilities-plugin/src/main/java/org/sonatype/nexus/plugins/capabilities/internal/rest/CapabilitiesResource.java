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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.sonatype.nexus.capability.support.CapabilitiesPlugin;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityNotFoundException;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;
import org.sonatype.sisu.siesta.common.error.ObjectNotFoundException;

import com.google.common.collect.Maps;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder.CapabilityReferenceFilter;

@Named
@Singleton
@Path(CapabilitiesResource.RESOURCE_URI)
public class CapabilitiesResource
    extends ComponentSupport
    implements Resource
{

  public static final String RESOURCE_URI = CapabilitiesPlugin.REST_PREFIX;

  private static final String $TYPE = "$type";

  private static final String $PROPERTY = "$p";

  private static final String $ENABLED = "$enabled";

  private static final String $ACTIVE = "$active";

  private static final String $INCLUDE_NOT_EXPOSED = "$includeNotExposed";

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public CapabilitiesResource(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
  }

  /**
   * Retrieve a list of capabilities currently configured in nexus.
   */
  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public CapabilitiesListResponseResource get(@QueryParam($TYPE) String type,
                                              @QueryParam($ENABLED) Boolean enabled,
                                              @QueryParam($ACTIVE) Boolean active,
                                              @QueryParam($INCLUDE_NOT_EXPOSED) Boolean includeNotExposed,
                                              @QueryParam($PROPERTY) List<String> properties)
  {
    final CapabilitiesListResponseResource result = new CapabilitiesListResponseResource();

    final Collection<? extends CapabilityReference> references = capabilityRegistry.get(
        buildFilter(type, enabled, active, includeNotExposed, properties)
    );

    for (final CapabilityReference reference : references) {
      result.addData(asCapabilityListItemResource(reference));
    }

    return result;
  }

  /**
   * Add a new capability.
   */
  @POST
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_JSON, APPLICATION_XML})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "create")
  public CapabilityStatusResponseResource post(final CapabilityRequestResource envelope)
      throws Exception
  {
    final CapabilityReference reference = capabilityRegistry.add(
        capabilityType(envelope.getData().getTypeId()),
        envelope.getData().isEnabled(),
        envelope.getData().getNotes(),
        asMap(envelope.getData().getProperties())
    );

    return asCapabilityStatusResponseResource(reference);
  }

  /**
   * Get the details of a capability.
   */
  @GET
  @Path("/{id}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public CapabilityResponseResource get(final @PathParam("id") String id) {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
    final CapabilityReference reference = capabilityRegistry.get(capabilityId);
    if (reference == null) {
      throw new ObjectNotFoundException(
          String.format("Capability with id '%s' was not found", capabilityId)
      );
    }

    return asCapabilityResponseResource(reference);
  }

  /**
   * Update the configuration of an existing capability.
   */
  @PUT
  @Path("/{id}")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "update")
  public CapabilityStatusResponseResource put(final @PathParam("id") String id,
                                              final CapabilityRequestResource envelope)
      throws Exception
  {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
    try {
      final CapabilityReference reference = capabilityRegistry.update(
          capabilityId,
          envelope.getData().isEnabled(),
          envelope.getData().getNotes(),
          asMap(envelope.getData().getProperties())
      );
      return asCapabilityStatusResponseResource(reference);
    }
    catch (CapabilityNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage(), e);
    }
  }

  /**
   * Delete an existing capability.
   */
  @DELETE
  @Path("/{id}")
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "delete")
  public void delete(final @PathParam("id") String id)
      throws Exception
  {
    try {
      capabilityRegistry.remove(capabilityIdentity(id));
    }
    catch (CapabilityNotFoundException e) {
      throw new ObjectNotFoundException(e.getMessage(), e);
    }
  }

  @GET
  @Path("/{id}/status")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public CapabilityStatusResponseResource getStatus(final @PathParam("id") String id) {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
    final CapabilityReference reference = capabilityRegistry.get(capabilityId);
    if (reference == null) {
      throw new ObjectNotFoundException(
          String.format("Capability with id '%s' was not found", capabilityId)
      );
    }
    return asCapabilityStatusResponseResource(reference);
  }

  /**
   * Update the configuration of an existing capability.
   */
  @PUT
  @Path("/{id}/status")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "update")
  public CapabilityStatusResponseResource put(final @PathParam("id") String id,
                                              final CapabilityStatusRequestResource envelope)
      throws Exception
  {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
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
      throw new ObjectNotFoundException(e.getMessage(), e);
    }

    return asCapabilityStatusResponseResource(reference);
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

  static CapabilityStatusResponseResource asCapabilityStatusResponseResource(final CapabilityReference reference) {
    checkNotNull(reference);

    final CapabilityStatusResponseResource status = new CapabilityStatusResponseResource();

    status.setData(asCapabilityListItemResource(reference));

    return status;
  }

  static CapabilityListItemResource asCapabilityListItemResource(final CapabilityReference reference) {
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

    return item;
  }


  private CapabilityReferenceFilter buildFilter(final String type,
                                                final Boolean enabled,
                                                final Boolean active,
                                                final Boolean includeNotExposed,
                                                final List<String> properties)
  {
    CapabilityReferenceFilter filter = CapabilityReferenceFilterBuilder.capabilities();
    if (type != null) {
      filter = filter.withType(capabilityType(type));
    }
    if (enabled != null) {
      filter = filter.enabled(enabled);
    }
    if (active != null) {
      filter = filter.active(active);
    }
    if (includeNotExposed == null || includeNotExposed) {
      filter = filter.includeNotExposed();
    }
    if (properties != null) {
      for (String property : properties) {
        String propertyName = property;
        String propertyValue = "*";
        if (property.contains(":")) {
          propertyName = property.substring(0, propertyName.indexOf(':'));
          if (propertyName.length() < property.length() - 1) {
            propertyValue = property.substring(propertyName.length() + 1);
          }
        }
        if ("*".equals(propertyValue)) {
          filter = filter.withBoundedProperty(propertyName);
        }
        else {
          filter = filter.withProperty(propertyName, propertyValue);
        }
      }
    }
    return filter;
  }

}

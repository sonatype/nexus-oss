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

import javax.annotation.Nullable;
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

import org.sonatype.nexus.capabilities.model.CapabilityStatusXO;
import org.sonatype.nexus.capabilities.model.CapabilityXO;
import org.sonatype.nexus.capabilities.model.PropertyXO;
import org.sonatype.nexus.capability.CapabilitiesPlugin;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityNotFoundException;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static org.sonatype.nexus.plugins.capabilities.CapabilityIdentity.capabilityIdentity;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder.CapabilityReferenceFilter;

/**
 * Capabilities REST resource.
 *
 * @since 2.7
 */
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
   * Get the details of a capability.
   */
  @GET
  @Path("/{id}")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public CapabilityXO get(final @PathParam("id") String id) {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
    final CapabilityReference reference = capabilityRegistry.get(capabilityId);
    if (reference == null) {
      throw new CapabilityNotFoundException(capabilityId);
    }
    return asCapability(reference);
  }

  @GET
  @Path("/{id}/status")
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public CapabilityStatusXO getInfo(final @PathParam("id") String id) {
    final CapabilityIdentity capabilityId = capabilityIdentity(id);
    final CapabilityReference reference = capabilityRegistry.get(capabilityId);
    if (reference == null) {
      throw new CapabilityNotFoundException(capabilityId);
    }
    return asCapabilityStatus(reference);
  }

  /**
   * Retrieve a list of capabilities currently configured in nexus.
   */
  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "read")
  public List<CapabilityStatusXO> get(@QueryParam($TYPE) String type,
                                      @QueryParam($ENABLED) Boolean enabled,
                                      @QueryParam($ACTIVE) Boolean active,
                                      @QueryParam($INCLUDE_NOT_EXPOSED) Boolean includeNotExposed,
                                      @QueryParam($PROPERTY) List<String> properties)
  {
    final Collection<? extends CapabilityReference> references = capabilityRegistry.get(
        buildFilter(type, enabled, active, includeNotExposed, properties)
    );

    return Lists.transform(Lists.newArrayList(references), new Function<CapabilityReference, CapabilityStatusXO>()
    {
      @Nullable
      @Override
      public CapabilityStatusXO apply(@Nullable final CapabilityReference input) {
        if (input == null) {
          return null;
        }
        return asCapabilityStatus(input);
      }
    });
  }

  /**
   * Add a new capability.
   */
  @POST
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_JSON, APPLICATION_XML})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "create")
  public CapabilityStatusXO post(final CapabilityXO capability)
      throws Exception
  {
    return asCapabilityStatus(
        capabilityRegistry.add(
            capabilityType(capability.getTypeId()),
            capability.isEnabled(),
            capability.getNotes(),
            asMap(capability.getProperties())
        )
    );
  }

  /**
   * Update the configuration of an existing capability.
   */
  @PUT
  @Path("/{id}")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "update")
  public CapabilityStatusXO put(final @PathParam("id") String id,
                                final CapabilityXO capability)
      throws Exception
  {
    return asCapabilityStatus(
        capabilityRegistry.update(
            capabilityIdentity(id),
            capability.isEnabled(),
            capability.getNotes(),
            asMap(capability.getProperties())
        )
    );
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
    capabilityRegistry.remove(capabilityIdentity(id));
  }

  /**
   * Enable an existing capability.
   */
  @PUT
  @Path("/{id}/enable")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "update")
  public CapabilityStatusXO enable(final @PathParam("id") String id)
      throws Exception
  {
    return asCapabilityStatus(capabilityRegistry.enable(capabilityIdentity(id)));
  }

  /**
   * Enable an existing capability.
   */
  @PUT
  @Path("/{id}/disable")
  @Consumes({APPLICATION_JSON, APPLICATION_XML})
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX + "update")
  public CapabilityStatusXO disable(final @PathParam("id") String id)
      throws Exception
  {
    return asCapabilityStatus(capabilityRegistry.disable(capabilityIdentity(id)));
  }

  static Map<String, String> asMap(final List<PropertyXO> properties) {
    final Map<String, String> map = Maps.newHashMap();

    if (properties != null) {
      for (final PropertyXO property : properties) {
        map.put(property.getKey(), property.getValue());
      }
    }

    return map;
  }

  static CapabilityStatusXO asCapabilityStatus(final CapabilityReference reference) {
    checkNotNull(reference);

    final CapabilityStatusXO capabilityStatus = new CapabilityStatusXO()
        .withCapability(asCapability(reference))
        .withTypeName(reference.context().descriptor().name())
        .withActive(reference.context().isActive())
        .withError(reference.context().hasFailure());

    try {
      capabilityStatus.setDescription(reference.capability().description());
    }
    catch (Exception ignore) {
      capabilityStatus.setDescription(null);
    }
    try {
      capabilityStatus.setStatus(reference.capability().status());
    }
    catch (Exception ignore) {
      capabilityStatus.setStatus(null);
    }
    capabilityStatus.setStateDescription(reference.context().stateDescription());

    return capabilityStatus;
  }

  private static CapabilityXO asCapability(final CapabilityReference reference) {
    CapabilityXO capability = new CapabilityXO()
        .withId(reference.context().id().toString())
        .withNotes(reference.context().notes())
        .withEnabled(reference.context().isEnabled())
        .withTypeId(reference.context().type().toString());

    if (reference.context().properties() != null) {
      for (final Map.Entry<String, String> entry : reference.context().properties().entrySet()) {
        capability.getProperties().add(new PropertyXO().withKey(entry.getKey()).withValue(entry.getValue()));
      }
    }
    return capability;
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
    if (includeNotExposed != null && includeNotExposed) {
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

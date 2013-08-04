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
import java.util.Collection;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.capabilities.model.XStreamConfigurator;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityListItemResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityStatusResponseResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asMap;
import static org.sonatype.nexus.plugins.capabilities.support.CapabilityReferenceFilterBuilder.CapabilityReferenceFilter;

@Named
@Singleton
@Path(CapabilitiesPlexusResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class CapabilitiesPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

  public static final String RESOURCE_URI = "/capabilities";

  private static final String $TYPE = "$type";

  private static final String $PROPERTY_PREFIX = "$p$";

  private static final String $ENABLED = "$enabled";

  private static final String $ACTIVE = "$active";

  private static final String $INCLUDE_NOT_EXPOSED = "$includeNotExposed";

  private final CapabilityRegistry capabilityRegistry;

  @Inject
  public CapabilitiesPlexusResource(final CapabilityRegistry capabilityRegistry) {
    this.capabilityRegistry = checkNotNull(capabilityRegistry);
    this.setModifiable(true);
  }

  @Override
  public void configureXStream(final XStream xstream) {
    XStreamConfigurator.configureXStream(xstream);
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
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:capabilities]");
  }

  /**
   * Retrieve a list of capabilities currently configured in nexus.
   */
  @Override
  @GET
  @ResourceMethodSignature(
      queryParams = {
          @QueryParam($TYPE),
          @QueryParam($ENABLED),
          @QueryParam($ACTIVE),
          @QueryParam($INCLUDE_NOT_EXPOSED),
          @QueryParam($PROPERTY_PREFIX + "{property name}")
      },
      output = CapabilitiesListResponseResource.class
  )
  public Object get(final Context context, final Request request, final Response response, final Variant variant)
      throws ResourceException
  {
    final CapabilitiesListResponseResource result = new CapabilitiesListResponseResource();

    final Collection<? extends CapabilityReference> references = capabilityRegistry.get(
        buildFilter(request.getResourceRef().getQueryAsForm())
    );

    for (final CapabilityReference reference : references) {
      result.addData(
          asCapabilityListItemResource(
              reference,
              createChildReference(request, this, reference.context().id().toString()).toString()
          )
      );
    }

    return result;
  }

  /**
   * Add a new capability.
   */
  @Override
  @POST
  @ResourceMethodSignature(
      input = CapabilityRequestResource.class,
      output = CapabilityStatusResponseResource.class
  )
  public Object post(final Context context, final Request request, final Response response, final Object payload)
      throws ResourceException
  {
    final CapabilityRequestResource envelope = (CapabilityRequestResource) payload;
    try {
      final CapabilityReference reference = capabilityRegistry.add(
          capabilityType(envelope.getData().getTypeId()),
          envelope.getData().isEnabled(),
          envelope.getData().getNotes(),
          asMap(envelope.getData().getProperties())
      );

      return asCapabilityStatusResponseResource(
          reference,
          createChildReference(request, this, reference.context().id().toString()).toString()
      );
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

  private CapabilityReferenceFilter buildFilter(final Form queryAsForm) {
    CapabilityReferenceFilter filter = CapabilityReferenceFilterBuilder.capabilities();
    final Set<String> paramNames = queryAsForm.getNames();
    if (paramNames != null) {
      for (final String paramName : paramNames) {
        final Parameter parameter = queryAsForm.getFirst(paramName);
        if ($TYPE.equals(paramName)) {
          if (parameter != null) {
            filter = filter.withType(capabilityType(parameter.getValue()));
          }
        }
        else if ($ENABLED.equals(paramName)) {
          if (parameter != null) {
            filter = filter.enabled(Boolean.valueOf(parameter.getValue()));
          }
          else {
            filter = filter.enabled();
          }
        }
        else if ($ACTIVE.equals(paramName)) {
          if (parameter != null) {
            filter = filter.active(Boolean.valueOf(parameter.getValue()));
          }
          else {
            filter = filter.active();
          }
        }
        else if ($INCLUDE_NOT_EXPOSED.equals(paramName)) {
          if (parameter == null || Boolean.valueOf(parameter.getValue())) {
            filter = filter.includeNotExposed();
          }
        }
        else if (paramName.startsWith($PROPERTY_PREFIX) && paramName.length() > $PROPERTY_PREFIX.length()) {
          final String propertyKey = paramName.substring($PROPERTY_PREFIX.length());
          if (parameter == null || "*".equals(parameter.getValue())) {
            filter = filter.withBoundedProperty(propertyKey);
          }
          else {
            filter = filter.withProperty(propertyKey, parameter.getValue());
          }
        }
      }
    }
    return filter;
  }

}

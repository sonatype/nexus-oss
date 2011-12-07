/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.internal.rest;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapabilityProperty;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityListItemResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Singleton
@Path( CapabilityPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class CapabilityPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

    public static final String CAPABILITIES_ID_KEY = "capabilityId";

    public static final String RESOURCE_URI = "/capabilities/{" + CAPABILITIES_ID_KEY + "}";

    private CapabilityConfiguration capabilitiesConfiguration;

    private CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private CapabilityRegistry capabilityRegistry;

    // TODO get rid of this constructor as it is here because enunciate plugin fails without a default constructor
    public CapabilityPlexusResource()
    {
    }

    @Inject
    public CapabilityPlexusResource( final CapabilityConfiguration capabilitiesConfiguration,
                                     final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
                                     final CapabilityRegistry capabilityRegistry )
    {
        this.capabilitiesConfiguration = checkNotNull( capabilitiesConfiguration );
        this.capabilityDescriptorRegistry = checkNotNull( capabilityDescriptorRegistry );
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new CapabilityRequestResource();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/capabilities/*", "authcBasic,perms[nexus:capabilities]" );
    }

    /**
     * Get the details of a capability.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( CapabilityPlexusResource.CAPABILITIES_ID_KEY ) },
                              output = CapabilityResponseResource.class )
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        try
        {
            final String capabilityId = getCapabilityId( request );
            final CCapability capability = capabilitiesConfiguration.get( capabilityId );
            if ( capability == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, String.format(
                    "Cannot find a capability with specified if of %s", capabilityId ) );
            }

            final CapabilityResponseResource result = asCapabilityResponseResource( capability );
            return result;
        }
        catch ( final Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL,
                                         "Could not manage capabilities configuration persistence store" );
        }
    }

    /**
     * Update the configuration of an existing capability.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( CapabilityPlexusResource.CAPABILITIES_ID_KEY ) },
                              input = CapabilityRequestResource.class, output = CapabilityStatusResponseResource.class )
    public Object put( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final CapabilityRequestResource envelope = (CapabilityRequestResource) payload;
        final CCapability capability = asCCapability( envelope.getData() );
        try
        {
            capabilitiesConfiguration.update( capability );
            capabilitiesConfiguration.save();

            final CapabilityStatusResponseResource result = asCapabilityStatusResponseResource(
                capability,
                createChildReference( request, this, capability.getId() ).toString(),
                capabilityDescriptorRegistry,
                capabilityRegistry
            );
            return result;
        }
        catch ( final InvalidConfigurationException e )
        {
            handleConfigurationException( e );
            return null;
        }
        catch ( final IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL,
                                         "Could not manage capabilities configuration persistence store" );
        }
    }

    /**
     * Delete an existing capability.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( CapabilityPlexusResource.CAPABILITIES_ID_KEY ) } )
    public void delete( final Context context, final Request request, final Response response )
        throws ResourceException
    {
        try
        {
            capabilitiesConfiguration.remove( getCapabilityId( request ) );
            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( final InvalidConfigurationException e )
        {
            handleConfigurationException( e );
        }
        catch ( final IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL,
                                         "Could not manage capabilities configuration persistence store" );
        }
    }

    static CCapability asCCapability( final CapabilityResource resource )
    {
        assert resource != null : "Resource cannot be null";

        final CCapability capability = new CCapability();

        capability.setId( resource.getId() );
        capability.setNotes( resource.getNotes() );
        capability.setEnabled( resource.isEnabled() );
        capability.setTypeId( resource.getTypeId() );

        if ( resource.getProperties() != null )
        {
            for ( final CapabilityPropertyResource propery : resource.getProperties() )
            {
                final CCapabilityProperty capabilityProp = new CCapabilityProperty();
                capabilityProp.setKey( propery.getKey() );
                capabilityProp.setValue( propery.getValue() );

                capability.addProperty( capabilityProp );
            }
        }

        return capability;
    }

    private static CapabilityResponseResource asCapabilityResponseResource( final CCapability capability )
    {
        assert capability != null : "Capability cannot be null";

        final CapabilityResource resource = new CapabilityResource();

        resource.setId( capability.getId() );
        resource.setNotes( capability.getNotes() );
        resource.setEnabled( capability.isEnabled() );
        resource.setTypeId( capability.getTypeId() );

        if ( capability.getProperties() != null )
        {
            for ( final CCapabilityProperty propery : capability.getProperties() )
            {
                final CapabilityPropertyResource resourceProp = new CapabilityPropertyResource();
                resourceProp.setKey( propery.getKey() );
                resourceProp.setValue( propery.getValue() );

                resource.addProperty( resourceProp );
            }
        }

        final CapabilityResponseResource response = new CapabilityResponseResource();
        response.setData( resource );

        return response;
    }

    static CapabilityStatusResponseResource asCapabilityStatusResponseResource(
        final CCapability capability,
        final String uri,
        final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
        final CapabilityRegistry capabilityRegistry )
    {
        assert capability != null : "Capability cannot be null";

        final CapabilityStatusResponseResource status = new CapabilityStatusResponseResource();

        status.setData( asCapabilityListItemResource(
            capability, uri, capabilityDescriptorRegistry, capabilityRegistry )
        );

        return status;
    }

    static CapabilityListItemResource asCapabilityListItemResource(
        final CCapability capability,
        final String uri,
        final CapabilityDescriptorRegistry capabilityDescriptorRegistry,
        final CapabilityRegistry capabilityRegistry )
    {
        assert capability != null : "Capability cannot be null";

        final CapabilityListItemResource item = new CapabilityListItemResource();
        item.setId( capability.getId() );
        item.setDescription( capability.getDescription() );
        item.setNotes( capability.getNotes() );
        item.setEnabled( capability.isEnabled() );
        item.setTypeId( capability.getTypeId() );

        final CapabilityDescriptor descriptor = capabilityDescriptorRegistry.get( capability.getTypeId() );
        item.setTypeName( descriptor == null ? "" : descriptor.name() );

        final CapabilityReference reference = capabilityRegistry.get( capability.getId() );
        item.setActive( reference != null && reference.isActive() );
        if ( reference != null )
        {
            item.setStateDescription( reference.stateDescription() );
        }

        item.setResourceURI( uri );

        return item;
    }

    private String getCapabilityId( final Request request )
    {
        return request.getAttributes().get( CAPABILITIES_ID_KEY ).toString();
    }

}

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
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityStatusResponseResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.getCapabilityId;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Singleton
@Path( CapabilityStatusPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class CapabilityStatusPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

    public static final String CAPABILITIES_ID_KEY = "capabilityId";

    public static final String RESOURCE_URI = "/capabilities/{" + CAPABILITIES_ID_KEY + "}/status";

    private CapabilityConfiguration capabilitiesConfiguration;

    private CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    private CapabilityRegistry capabilityRegistry;

    // TODO get rid of this constructor as it is here because enunciate plugin fails without a default constructor
    public CapabilityStatusPlexusResource()
    {
    }

    @Inject
    public CapabilityStatusPlexusResource( final CapabilityConfiguration capabilitiesConfiguration,
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
        return new CapabilityStatusRequestResource();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/capabilities/*/status", "authcBasic,perms[nexus:capabilities]" );
    }

    /**
     * Update the configuration of an existing capability.
     */
    @Override
    @PUT
    public Object put( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final CapabilityStatusRequestResource envelope = (CapabilityStatusRequestResource) payload;
        final String capabilityId = getCapabilityId( request );
        try
        {
            final CCapability capability = capabilitiesConfiguration.get( capabilityId );
            if ( capability == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, String.format(
                    "Cannot find a capability with specified if of %s", capabilityId ) );
            }
            capability.setEnabled( envelope.getData().isEnabled() );
            capabilitiesConfiguration.update( capability );
            capabilitiesConfiguration.save();

            final CapabilityStatusResponseResource result = asCapabilityStatusResponseResource(
                capability,
                request.getResourceRef().toString(),
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

}

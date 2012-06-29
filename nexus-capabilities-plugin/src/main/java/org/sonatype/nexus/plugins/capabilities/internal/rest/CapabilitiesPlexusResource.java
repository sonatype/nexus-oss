/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityListItemResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asCapabilityStatusResponseResource;
import static org.sonatype.nexus.plugins.capabilities.internal.rest.CapabilityPlexusResource.asMap;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.CapabilityReference;
import org.sonatype.nexus.plugins.capabilities.CapabilityRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.model.XStreamConfigurator;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import com.thoughtworks.xstream.XStream;

@Singleton
@Path( CapabilitiesPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class CapabilitiesPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

    public static final String RESOURCE_URI = "/capabilities";

    private final CapabilityRegistry capabilityRegistry;

    @Inject
    public CapabilitiesPlexusResource( final CapabilityRegistry capabilityRegistry )
    {
        this.capabilityRegistry = checkNotNull( capabilityRegistry );
        this.setModifiable( true );
    }

    @Override
    public void configureXStream( final XStream xstream )
    {
        XStreamConfigurator.configureXStream( xstream );
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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:capabilities]" );
    }

    /**
     * Retrieve a list of capabilities currently configured in nexus.
     */
    @Override
    @GET
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final boolean includeHidden = Boolean.parseBoolean(
            request.getResourceRef().getQueryAsForm().getFirstValue( "includeHidden", true, "false" )
        );
        final boolean includeNotExposed = Boolean.parseBoolean(
            request.getResourceRef().getQueryAsForm().getFirstValue( "includeNotExposed", true, "false" )
        );
        final CapabilitiesListResponseResource result = new CapabilitiesListResponseResource();

        for ( final CapabilityReference reference : capabilityRegistry.getAll() )
        {
            if ( ( includeNotExposed || reference.context().descriptor().isExposed() ) &&
                ( includeHidden || !reference.context().descriptor().isHidden() ) )
            {
                result.addData(
                    asCapabilityListItemResource(
                        reference,
                        createChildReference( request, this, reference.context().id().toString() ).toString()
                    )
                );
            }
        }

        return result;
    }

    /**
     * Add a new capability.
     */
    @Override
    @POST
    public Object post( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final CapabilityRequestResource envelope = (CapabilityRequestResource) payload;
        try
        {
            final CapabilityReference reference = capabilityRegistry.add(
                capabilityType( envelope.getData().getTypeId() ),
                envelope.getData().isEnabled(),
                envelope.getData().getNotes(),
                asMap( envelope.getData().getProperties() )
            );

            return asCapabilityStatusResponseResource(
                reference,
                createChildReference( request, this, reference.context().id().toString() ).toString()
            );
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

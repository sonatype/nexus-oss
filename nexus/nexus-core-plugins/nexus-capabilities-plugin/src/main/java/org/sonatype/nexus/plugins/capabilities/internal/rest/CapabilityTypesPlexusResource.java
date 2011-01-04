/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityFormFieldResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResourceResponse;
import org.sonatype.nexus.rest.formfield.AbstractFormFieldResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Singleton
@Path( CapabilityTypesPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class CapabilityTypesPlexusResource
    extends AbstractFormFieldResource
    implements PlexusResource
{

    public static final String RESOURCE_URI = "/capabilityTypes";

    private CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    // TODO get rid of this constructor as it is here because enunciate plugin fails without a default constructor
    public CapabilityTypesPlexusResource()
    {
    }
    
    @Inject
    public CapabilityTypesPlexusResource( final CapabilityDescriptorRegistry capabilityDescriptorRegistry )
    {
        this.capabilityDescriptorRegistry = capabilityDescriptorRegistry;
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:capabilityTypes]" );
    }

    /**
     * Retrieve a list of capability types available.
     */
    @Override
    @GET
    @ResourceMethodSignature( output = CapabilityTypeResourceResponse.class )
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final CapabilityTypeResourceResponse result = asCapabilityTypeResourceResponse();

        return result;
    }

    private CapabilityTypeResourceResponse asCapabilityTypeResourceResponse()
    {
        final CapabilityTypeResourceResponse response = new CapabilityTypeResourceResponse();

        final CapabilityDescriptor[] descriptors = capabilityDescriptorRegistry.getAll();

        if ( descriptors != null )
        {
            for ( final CapabilityDescriptor capabilityDescriptor : descriptors )
            {
                final CapabilityTypeResource capabilityTypeResource = new CapabilityTypeResource();
                capabilityTypeResource.setId( capabilityDescriptor.id() );
                capabilityTypeResource.setName( capabilityDescriptor.name() );

                response.addData( capabilityTypeResource );

                final List<FormField> formFields = capabilityDescriptor.formFields();

                capabilityTypeResource.setFormFields( (List<CapabilityFormFieldResource>) formFieldToDTO( formFields,
                                                                                                          CapabilityFormFieldResource.class ) );

            }
        }

        return response;
    }
}

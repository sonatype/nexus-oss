package org.sonatype.nexus.plugins.capabilities.internal.rest;

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
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityPropertyDescriptor;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypePropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResourceResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Singleton
@Path( CapabilityTypesPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class CapabilityTypesPlexusResource
    extends AbstractNexusPlexusResource
    implements PlexusResource
{

    public static final String RESOURCE_URI = "/capabilityTypes";

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

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

                final CapabilityPropertyDescriptor[] propertyDescriptors = capabilityDescriptor.propertyDescriptors();
                if ( propertyDescriptors != null )
                {
                    for ( final CapabilityPropertyDescriptor capabilityPropertyDescriptor : propertyDescriptors )
                    {
                        final CapabilityTypePropertyResource capabilityTypePropertyResource =
                            new CapabilityTypePropertyResource();
                        capabilityTypePropertyResource.setId( capabilityPropertyDescriptor.id() );
                        capabilityTypePropertyResource.setName( capabilityPropertyDescriptor.name() );
                        capabilityTypePropertyResource.setType( capabilityPropertyDescriptor.type() );
                        capabilityTypePropertyResource.setRequired( capabilityPropertyDescriptor.isRequired() );
                        capabilityTypePropertyResource.setRegexValidation( capabilityPropertyDescriptor.regexValidation() );
                        capabilityTypeResource.addProperty( capabilityTypePropertyResource );
                    }
                }
            }
        }

        return response;
    }
}

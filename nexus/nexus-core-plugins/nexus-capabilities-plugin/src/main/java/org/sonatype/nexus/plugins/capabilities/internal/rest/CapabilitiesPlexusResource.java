package org.sonatype.nexus.plugins.capabilities.internal.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.config.CapabilityConfiguration;
import org.sonatype.nexus.plugins.capabilities.internal.config.persistence.CCapability;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityPropertyResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityRequestResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityStatusResponseResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilitiesListResponseResource;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

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

    // @Inject
    private final CapabilityConfiguration capabilitiesConfiguration;

    private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

    @Inject
    public CapabilitiesPlexusResource( final CapabilityConfiguration capabilitiesConfiguration,
                                   final CapabilityDescriptorRegistry capabilityDescriptorRegistry )
    {
        this.capabilitiesConfiguration = capabilitiesConfiguration;
        this.capabilityDescriptorRegistry = capabilityDescriptorRegistry;
        this.setModifiable( true );
    }

    @Override
    public void configureXStream( final XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.registerConverter( new CapabilityPropertyResourceConverter(
            xstream.getMapper(),
            xstream.getReflectionProvider() ), XStream.PRIORITY_VERY_HIGH );

        xstream.processAnnotations( CapabilityRequestResource.class );
        xstream.processAnnotations( CapabilityResponseResource.class );
        xstream.processAnnotations( CapabilitiesListResponseResource.class );
        xstream.processAnnotations( CapabilityStatusResponseResource.class );

        xstream.registerLocalConverter( CapabilityResource.class, "properties", new AliasingListConverter(
            CapabilityPropertyResource.class, "featurre-property" ) );
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
    @ResourceMethodSignature( output = CapabilitiesListResponseResource.class )
    public Object get( final Context context, final Request request, final Response response, final Variant variant )
        throws ResourceException
    {
        final CapabilitiesListResponseResource result = new CapabilitiesListResponseResource();

        try
        {
            for ( final CCapability capability : capabilitiesConfiguration.getAll() )
            {
                result.addData( CapabilityPlexusResource.asCapabilityListItemResource( capability, createChildReference(
                    request, this, capability.getId() ).toString(), capabilityDescriptorRegistry ) );
            }
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

        return result;
    }

    /**
     * Add a new capability.
     */
    @Override
    @POST
    @ResourceMethodSignature( input = CapabilityRequestResource.class, output = CapabilityStatusResponseResource.class )
    public Object post( final Context context, final Request request, final Response response, final Object payload )
        throws ResourceException
    {
        final CapabilityRequestResource envelope = (CapabilityRequestResource) payload;
        final CCapability capability = CapabilityPlexusResource.asCCapability( envelope.getData() );
        try
        {
            capabilitiesConfiguration.add( capability );
            capabilitiesConfiguration.save();

            final CapabilityStatusResponseResource result =
                CapabilityPlexusResource.asCapabilityStatusResponseResource( capability, createChildReference( request, this,
                capability.getId() ).toString(), capabilityDescriptorRegistry );
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

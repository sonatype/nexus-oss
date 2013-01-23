package org.sonatype.nexus.rest.mwl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryConfig;
import org.sonatype.nexus.rest.model.WLConfigMessage;
import org.sonatype.nexus.rest.model.WLConfigMessageWrapper;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * WL Configuration REST resource, usable only on Maven Proxy repositories.
 * 
 * @author cstamas
 * @since 2.4
 */
@Component( role = PlexusResource.class, hint = "WLConfigResource" )
@Path( WLConfigResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class WLConfigResource
    extends WLResourceSupport
{
    /**
     * REST resource URI.
     */
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}/wl/config";

    @Override
    public Object getPayloadInstance()
    {
        return new WLConfigMessageWrapper();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
     * Returns the current WL configuration for given repository.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( REPOSITORY_ID_KEY ) }, output = WLConfigMessageWrapper.class )
    public WLConfigMessageWrapper get( final Context context, final Request request, final Response response,
                                       final Variant variant )
        throws ResourceException
    {
        final MavenProxyRepository mavenProxyRepository = getMavenRepository( request, MavenProxyRepository.class );
        final WLDiscoveryConfig config = getWLManager().getRemoteDiscoveryConfig( mavenProxyRepository );
        final WLConfigMessage payload = new WLConfigMessage();
        if ( config.isEnabled() )
        {
            payload.setDiscoveryEnabled( true );
            payload.setDiscoveryInterval( TimeUnit.MILLISECONDS.toHours( config.getDiscoveryInterval() ) );
        }
        else
        {
            payload.setDiscoveryEnabled( false );
            payload.setDiscoveryInterval( -1 );
        }
        final WLConfigMessageWrapper responseNessage = new WLConfigMessageWrapper();
        responseNessage.setData( payload );
        return responseNessage;
    }

    /**
     * Sets the WL configuration for given repository.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( REPOSITORY_ID_KEY ) }, input = WLConfigMessageWrapper.class, output = WLConfigMessageWrapper.class )
    public WLConfigMessageWrapper put( final Context context, final Request request, final Response response,
                                       final Object payload )
        throws ResourceException
    {
        try
        {
            final MavenProxyRepository mavenProxyRepository = getMavenRepository( request, MavenProxyRepository.class );
            final WLConfigMessageWrapper wrapper = WLConfigMessageWrapper.class.cast( payload );
            final WLDiscoveryConfig config =
                new WLDiscoveryConfig(
                    wrapper.getData().isDiscoveryEnabled(),
                    wrapper.getData().isDiscoveryEnabled() ? TimeUnit.HOURS.toMillis( wrapper.getData().getDiscoveryInterval() )
                        : -1L );
            getWLManager().setRemoteDiscoveryConfig( mavenProxyRepository, config );
            return wrapper;
        }
        catch ( ClassCastException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Message not recognized!", e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }
}

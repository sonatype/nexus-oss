package org.sonatype.nexus.rest.mwl;

import java.io.IOException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryConfig;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus;
import org.sonatype.nexus.proxy.maven.wl.WLDiscoveryStatus.DStatus;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus;
import org.sonatype.nexus.proxy.maven.wl.WLPublishingStatus.PStatus;
import org.sonatype.nexus.proxy.maven.wl.WLStatus;
import org.sonatype.nexus.rest.RepositoryURLBuilder;
import org.sonatype.nexus.rest.model.WLDiscoveryStatusMessage;
import org.sonatype.nexus.rest.model.WLStatusMessage;
import org.sonatype.nexus.rest.model.WLStatusMessageWrapper;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * WL Status REST resource.
 * 
 * @author cstamas
 * @since 2.4
 */
@Component( role = PlexusResource.class, hint = "WLStatusResource" )
@Path( WLStatusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class WLStatusResource
    extends WLResourceSupport
{
    /**
     * REST resource URI.
     */
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}/wl";

    @Requirement( hint = "RestletRepositoryUrlBuilder" )
    private RepositoryURLBuilder repositoryURLBuilder;

    @Override
    public Object getPayloadInstance()
    {
        return new WLStatusMessageWrapper();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( REPOSITORY_ID_KEY ) }, output = WLStatusMessageWrapper.class )
    public WLStatusMessageWrapper get( final Context context, final Request request, final Response response,
                                       final Variant variant )
        throws ResourceException
    {
        final MavenRepository mavenRepository = getMavenRepository( request, MavenRepository.class );
        final WLStatus status = getWLManager().getStatusFor( mavenRepository );
        final WLStatusMessage payload = new WLStatusMessage();
        payload.setPublished( PStatus.PUBLISHED == status.getPublishingStatus().getStatus() );
        payload.setPublishedMessage( status.getPublishingStatus().getLastPublishedMessage() );
        if ( payload.isPublished() )
        {
            final WLPublishingStatus pstatus = status.getPublishingStatus();
            payload.setPublishedTimestamp( pstatus.getLastPublishedTimestamp() );
            if ( pstatus.getLastPublishedFilePath() != null )
            {
                final String repositoryUrl = repositoryURLBuilder.getExposedRepositoryContentUrl( mavenRepository );
                if ( repositoryUrl != null )
                {
                    payload.setPublishedUrl( repositoryUrl + pstatus.getLastPublishedFilePath() );
                }
            }
        }
        else
        {
            payload.setPublishedTimestamp( -1 );
            payload.setPublishedUrl( null );
        }
        if ( DStatus.NOT_A_PROXY == status.getDiscoveryStatus().getStatus() )
        {
            payload.setDiscoveryStatus( null );
        }
        else
        {
            final WLDiscoveryStatus dstatus = status.getDiscoveryStatus();
            payload.setDiscoveryStatus( new WLDiscoveryStatusMessage() );
            if ( DStatus.DISABLED == status.getDiscoveryStatus().getStatus() )
            {
                payload.getDiscoveryStatus().setEnabled( false );
            }
            else if ( DStatus.ENABLED == status.getDiscoveryStatus().getStatus() )
            {
                payload.getDiscoveryStatus().setEnabled( true );
                // no last run, so nothing else to say
            }
            else
            {
                final MavenProxyRepository mavenProxyRepository =
                    getMavenRepository( request, MavenProxyRepository.class );
                final WLDiscoveryConfig config = getWLManager().getRemoteDiscoveryConfig( mavenProxyRepository );
                payload.getDiscoveryStatus().setEnabled( config.isEnabled() );
                payload.getDiscoveryStatus().setUpdateInterval( config.getDiscoveryInterval() );
                // last- messages
                payload.getDiscoveryStatus().setLastStatus( dstatus.getStatus().name() );
                payload.getDiscoveryStatus().setLastStrategy( dstatus.getLastDiscoveryStrategy() );
                payload.getDiscoveryStatus().setLastMessage( dstatus.getLastDiscoveryMessage() );
                payload.getDiscoveryStatus().setLastRunTimestamp( dstatus.getLastDiscoveryTimestamp() );
            }
        }

        final WLStatusMessageWrapper responseNessage = new WLStatusMessageWrapper();
        responseNessage.setData( payload );
        return responseNessage;
    }

    /**
     * Force updates WL for given proxy repository. If invoked for non-Maven proxy repository, response is Bad Request.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( REPOSITORY_ID_KEY ) } )
    public void delete( final Context context, final Request request, final Response response )
        throws ResourceException
    {
        try
        {
            final MavenProxyRepository mavenRepository = getMavenRepository( request, MavenProxyRepository.class );
            getWLManager().updateWhitelist( mavenRepository );
            // currently this happens synchronously, but it is the status that will reveal real outcome of the operation
            response.setStatus( Status.SUCCESS_ACCEPTED );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }
    }
}

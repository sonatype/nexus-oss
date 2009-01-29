package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Client;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "remote" strategy, uses HTTP GET for information fetch from the remoteUrl. Note: this class uses Restlet
 * Client implementation to do it. Note: this implementation will follow redirects, up to 3 times.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "http-get" )
public class HttpGetDiscoveryStrategy
    extends AbstractRemoteDiscoveryStrategy
{
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        Response response = handleRequest( getRemoteUrl( request ), 3 );

        if ( response.getStatus().isSuccess() && response.isEntityAvailable() )
        {
            Representation output = response.getEntity();

            dr.setVersion( output.getText() );

            dr.setSuccessful( true );
        }

        return dr;
    }

    protected Response handleRequest( String url, int retries )
    {
        // Prepare the request
        Request rr = getRestRequest( url );

        // Handle it using an HTTP client connector
        Client client = new Client( Protocol.HTTP );
        Response response = client.handle( rr );

        if ( response.getStatus().isRedirection() && retries > 0 )
        {
            return handleRequest( response.getLocationRef().toString(), retries-- );
        }

        return response;
    }

    protected String getRemoteUrl( DiscoveryRequest request )
    {
        return request.getLvoKey().getRemoteUrl();
    }

    protected Request getRestRequest( String url )
    {
        Request rr = new Request( Method.GET, url );

        rr.setReferrerRef( getNexus().getBaseUrl() );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( formatUserAgent() );

        rr.setClientInfo( ci );

        return rr;
    }
}

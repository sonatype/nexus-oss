package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.error.reporting.NexusProxyServerConfigurator;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.RestApiConfiguration;

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
    @Requirement
    private RestApiConfiguration restApiConfiguration;
    
    @Requirement
    private NexusConfiguration nexusConfig;
    
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        InputStream is = handleRequest( getRemoteUrl( request ) );

        if ( is != null )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
    
                dr.setVersion( reader.readLine() );
    
                dr.setSuccessful( true );
            }
            finally
            {
                IOUtil.close( is );
            }
        }

        return dr;
    }

    protected InputStream handleRequest( String url ) 
        throws IOException
    {
        HttpClient client = new HttpClient();
        
        new NexusProxyServerConfigurator( nexusConfig.getGlobalRemoteStorageContext(), getLogger() ).applyToClient( client );
        
        client.getHttpConnectionManager().getParams().setConnectionTimeout( 10000 );
        
        GetMethod method = null;        
        InputStream is = null;
        
        try
        {
            method = new GetMethod( url );
            method.setFollowRedirects( true );
            
            int status = client.executeMethod( method );
            
            if ( HttpStatus.SC_OK == status )
            {
                is = method.getResponseBodyAsStream();
            }
        }
        finally
        {
            if ( method != null )
            {
                method.releaseConnection();
            }
        }
        
        return is;
    }

    protected String getRemoteUrl( DiscoveryRequest request )
    {
        return request.getLvoKey().getRemoteUrl();
    }

    protected Request getRestRequest( String url )
    {
        Request rr = new Request( Method.GET, url );

        rr.setReferrerRef( restApiConfiguration.getBaseUrl() );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( formatUserAgent() );

        rr.setClientInfo( ci );

        return rr;
    }
}

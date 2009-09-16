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
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.error.reporting.NexusProxyServerConfigurator;
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
    @Requirement
    private GlobalRestApiSettings restApiSettings;
    
    @Requirement
    private NexusConfiguration nexusConfig;
    
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        RequestResult response = handleRequest( getRemoteUrl( request ) );

        if ( response != null )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new InputStreamReader( response.getInputStream() ) );
    
                dr.setVersion( reader.readLine() );
    
                dr.setSuccessful( true );
            }
            finally
            {
                response.close();
            }
        }

        return dr;
    }

    protected RequestResult handleRequest( String url )
    {
        HttpClient client = new HttpClient();
        
        new NexusProxyServerConfigurator( nexusConfig.getGlobalRemoteStorageContext(), getLogger() ).applyToClient( client );
        
        GetMethod method = new GetMethod( url );
        
        RequestResult result = null;
        
        try
        {
            int status = client.executeMethod( method );
            
            if ( HttpStatus.SC_OK == status )
            {
                result = new RequestResult( method );
            }
        }
        catch ( IOException e )
        {
            getLogger().debug( "Error retrieving lvo data", e );
        }
        
        return result;
    }

    protected String getRemoteUrl( DiscoveryRequest request )
    {
        return request.getLvoKey().getRemoteUrl();
    }

    protected Request getRestRequest( String url )
    {
        Request rr = new Request( Method.GET, url );

        rr.setReferrerRef( restApiSettings.getBaseUrl() );

        ClientInfo ci = new ClientInfo();

        ci.setAgent( formatUserAgent() );

        rr.setClientInfo( ci );

        return rr;
    }
    
    protected static final class RequestResult
    {
        private InputStream is;
        private GetMethod method;
        
        public RequestResult( GetMethod method ) 
            throws IOException
        {
            this.is = method.getResponseBodyAsStream();
            this.method = method;
        }
        
        public InputStream getInputStream()
        {
            return this.is;
        }
        
        public void close()
        {
            IOUtil.close( is );
            
            if ( this.method != null )
            {
                this.method.releaseConnection();
            }
        }
    }
}

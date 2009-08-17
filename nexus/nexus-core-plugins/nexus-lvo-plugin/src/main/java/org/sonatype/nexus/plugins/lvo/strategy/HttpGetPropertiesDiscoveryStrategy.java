package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * This is a "remote" strategy, uses HTTP GET to get a Java properties file and get filtered keys from there. It extends
 * the HttpGetDiscoveryStrategy, and assumes that a GETted content is a java.util.Properties file.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "http-get-properties" )
public class HttpGetPropertiesDiscoveryStrategy
    extends HttpGetDiscoveryStrategy
{
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        RequestResult response = handleRequest( getRemoteUrl( request ) );

        if ( response != null )
        {
            Properties properties = new Properties();

            try
            {
                properties.load( response.getInputStream() );
            }
            finally
            {
                response.close();
            }

            String keyPrefix = request.getKey() + ".";

            // repack it into response
            for ( Object key : properties.keySet() )
            {
                String keyString = key.toString();

                if ( keyString.startsWith( keyPrefix ) )
                {
                    dr.getResponse().put( key.toString().substring( keyPrefix.length() ), properties.get( key ) );

                    dr.setSuccessful( true );
                }
            }
        }

        return dr;
    }
}

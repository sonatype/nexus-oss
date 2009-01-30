package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.Response;
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
        Response response = handleRequest( getRemoteUrl( request ), 3 );

        if ( response.getStatus().isSuccess() && response.isEntityAvailable() )
        {
            Properties properties = new Properties();

            InputStream content = response.getEntity().getStream();

            try
            {
                properties.load( content );
            }
            finally
            {
                IOUtil.close( content );
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

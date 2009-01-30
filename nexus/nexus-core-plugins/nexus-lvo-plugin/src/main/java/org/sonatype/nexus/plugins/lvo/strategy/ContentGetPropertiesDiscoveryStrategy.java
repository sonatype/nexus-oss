package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * This is a "local" strategy, uses Nexus content to get a Java properties file and get filtered keys from there.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "content-get-properties" )
public class ContentGetPropertiesDiscoveryStrategy
    extends ContentGetDiscoveryStrategy
{
    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException,
            IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        StorageFileItem response = handleRequest( request );

        if ( response != null )
        {
            Properties properties = new Properties();

            InputStream content = response.getInputStream();

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

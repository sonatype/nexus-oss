package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.XppDomDriver;

/**
 * This is a "remote" strategy, uses HTTP GET to get a remote LVO Plugin response. It extends the
 * HttpGetDiscoveryStrategy, and assumes that a GETted content is a LVO Plugin response.
 * 
 * @author cstamas
 */
@Component( role = DiscoveryStrategy.class, hint = "http-get-lvo" )
public class HttpGetLvoDiscoveryStrategy
    extends HttpGetDiscoveryStrategy
{
    private XStream xstream;

    protected XStream getXStream()
    {
        if ( xstream == null )
        {
            XStream xstream = new XStream( new XppDomDriver() );

            DiscoveryResponse.configureXStream( xstream );
        }

        return xstream;
    }

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
                DiscoveryResponse remoteResponse = (DiscoveryResponse) getXStream().fromXML( response.getInputStream() );

                return remoteResponse;
            }
            catch ( XStreamException e )
            {
                // handle gracefully, but only XStream problems!
                dr.setSuccessful( false );

                return dr;
            }
            finally
            {
                response.close();
            }
        }

        return dr;
    }
}

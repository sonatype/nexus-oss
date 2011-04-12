/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.lvo.strategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Future;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.ahc.AhcProvider;
import org.sonatype.nexus.plugins.lvo.DiscoveryRequest;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.DiscoveryStrategy;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

import com.ning.http.client.BodyDeferringAsyncHandler;
import com.ning.http.client.BodyDeferringAsyncHandler.BodyDeferringInputStream;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;

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
    private AhcProvider ahcProvider;

    public DiscoveryResponse discoverLatestVersion( DiscoveryRequest request )
        throws NoSuchRepositoryException, IOException
    {
        DiscoveryResponse dr = new DiscoveryResponse( request );

        // handle
        InputStream response = handleRequest( getRemoteUrl( request ) );

        if ( response != null )
        {
            try
            {
                BufferedReader reader = new BufferedReader( new InputStreamReader( response ) );

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

    protected InputStream handleRequest( String url )
    {
        Request request = new RequestBuilder().setFollowRedirects( true ).setUrl( url ).build();

        try
        {
            final PipedOutputStream po = new PipedOutputStream();

            final PipedInputStream pi = new PipedInputStream( po );

            final BodyDeferringAsyncHandler bdah = new BodyDeferringAsyncHandler( po );

            Future<Response> f = ahcProvider.getAsyncHttpClient().executeRequest( request, bdah );

            BodyDeferringInputStream result = new BodyDeferringInputStream( f, bdah, pi );

            if ( 200 == result.getAsapResponse().getStatusCode() )
            {
                return result;
            }
            else
            {
                result.close();

                return null;
            }
        }
        catch ( IOException e )
        {
            getLogger().debug( "Error retrieving lvo data", e );
        }
        catch ( InterruptedException e )
        {
            getLogger().debug( "Error retrieving lvo data", e );
        }

        return null;
    }

    protected String getRemoteUrl( DiscoveryRequest request )
    {
        return request.getLvoKey().getRemoteUrl();
    }
}

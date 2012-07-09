/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2812;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;

/**
 * This IT checks that previously retrieved P2 metadata is used from cache, even when the proxied P2 repository is unavailable.
 * (Nexus is configured to always go remote for the P2 proxy repository.)
 */
public class NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT()
    {
        super( "nxcm2812" );
    }

    @Test
    public void test()
        throws Exception
    {
        final String url = "content/repositories/" + getTestRepositoryId() + "/content.xml";

        // init local storage
        Response content = null;
        try
        {
            content = RequestFacade.sendMessage( url, Method.GET );
            assertThat( content.getEntity().getText(), containsString( "<?metadataRepository" ) );
            installAndVerifyP2Feature();
        }
        finally
        {
            RequestFacade.releaseResponse( content );
        }

        // invalidate remote repo
        replaceProxy();

        // check delivery from local storage
        try
        {
            content = RequestFacade.sendMessage( url, Method.GET );
            assertThat( content.getEntity().getText(), containsString( "<?metadataRepository" ) );
            installAndVerifyP2Feature();
        }
        finally
        {
            RequestFacade.releaseResponse( content );
        }
    }

    private void replaceProxy()
        throws Exception
    {
        final Handler handler = proxyServer.getServer().getHandler();
        proxyServer.stop();
        proxyServer.getServer().setHandler( new AbstractHandler()
        {
            @Override
            public void handle( String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response )
                throws IOException, ServletException
            {
                for ( final String path : P2Constants.METADATA_FILE_PATHS )
                {
                    if ( target.endsWith( path ) )
                    {
                        response.sendError( 503 );
                        return;
                    }
                }
                handler.handle( target, baseRequest, request, response );
            }
        } );
        proxyServer.start();
    }

}

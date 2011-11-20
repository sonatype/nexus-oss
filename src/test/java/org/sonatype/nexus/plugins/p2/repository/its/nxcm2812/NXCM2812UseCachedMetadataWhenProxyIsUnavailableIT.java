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
package org.sonatype.nexus.plugins.p2.repository.its.nxcm2812;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.AbstractHandler;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.plugins.p2.repository.P2Constants;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.testng.annotations.Test;

public class NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT
    extends AbstractNexusProxyP2IT
{

    public NXCM2812UseCachedMetadataWhenProxyIsUnavailableIT()
    {
        super( "nxcm2812" );
    }

    @Test
    public void test()
        throws IOException, Exception
    {
        final String url = "content/repositories/" + getTestRepositoryId() + "/content.xml";

        // init local storage
        final Response content = RequestFacade.sendMessage( url, Method.GET );
        assertThat( content.getEntity().getText(), containsString( "<?metadataRepository" ) );
        installAndVerifyP2Feature();

        // invalidate remote repo
        replaceProxy();

        // check delivery from local storage
        final Response content2 = RequestFacade.sendMessage( url, Method.GET );
        assertThat( content2.getEntity().getText(), containsString( "<?metadataRepository" ) );
        installAndVerifyP2Feature();
    }

    private void replaceProxy()
        throws Exception
    {
        final Handler handler = proxyServer.getServer().getHandler();
        proxyServer.stop();
        proxyServer.getServer().setHandler( new AbstractHandler()
        {

            @Override
            public void handle( final String target, final HttpServletRequest request,
                                final HttpServletResponse response, final int dispatch )
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
                handler.handle( target, request, response, dispatch );
            }
        } );
        proxyServer.start();
    }

}

/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.integrationtests.nexus4593;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.mortbay.jetty.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryStatusMessageUtil;
import org.sonatype.nexus.test.utils.ResponseMatchers;
import org.sonatype.nexus.test.utils.handler.ReturnErrorHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * IT testing that a remote answering with '403 Forbidden' will not be auto-blocked.
 *
 * @since 1.10.0
 */
public class Nexus4593NoAutoblockFor403IT
    extends AbstractNexusProxyIntegrationTest
{

    private Server errorServer;

    @AfterMethod
    public void stopServer()
        throws Exception
    {
        if ( errorServer != null )
        {
            errorServer.stop();
        }
    }

    @Test
    public void test()
        throws Exception
    {
        System.err.println( proxyPort );
        startErrorServer( 403, true );
        Thread.sleep( 300000 );
    }

    /**
     * Verify that a remote answering with '403 Forbidden' will not be auto-blocked.
     */
    @Test
    public void testNoAutoblockOn403()
        throws Exception
    {
        startErrorServer( 403 );

        try
        {
            downloadArtifact( GavUtil.newGav( "g", "a", "v" ), "target" );
        }
        catch ( IOException e )
        {
            // expected, remote will answer with 403
        }

        RepositoryStatusResource status = getStatus();
        assertThat( ProxyMode.valueOf( status.getProxyMode() ), is( ProxyMode.ALLOW ) );
    }

    /**
     * Verify that a remote answering with '401' will still be auto-blocked.
     * <p/>
     * This needs to be run after the 403-test-method, because this test will change repo status to auto blocked.
     */
    @Test(dependsOnMethods = "testNoAutoblockOn403")
    public void testAutoblockOn401()
        throws Exception
    {
        startErrorServer( 401 );

        try
        {
            downloadArtifact( GavUtil.newGav( "g", "a", "v" ), "target" );
        }
        catch ( IOException e )
        {
            // expected, remote will answer with 401
        }

        RepositoryStatusResource status = getStatus();

        assertThat( ProxyMode.valueOf( status.getProxyMode() ), is( ProxyMode.BLOCKED_AUTO ) );
    }

    private void startErrorServer( final int code )
        throws Exception
    {
        ServletServer server = (ServletServer) this.lookup( ServletServer.ROLE );
        server.stop();

        int port = server.getPort();

        errorServer = new Server( port );
        errorServer.setHandler( new ReturnErrorHandler( code ) );
        errorServer.start();
    }

    private RepositoryStatusResource getStatus()
        throws IOException
    {
        return new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML ).getStatus(
            getTestRepositoryId() );
    }
}

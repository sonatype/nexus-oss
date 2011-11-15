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
import static org.sonatype.tests.http.server.fluent.Behaviours.error;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.ErrorBehaviour;
import org.sonatype.tests.http.server.jetty.impl.JettyServerProvider;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * IT testing that a remote answering with '403 Forbidden' will not be auto-blocked.
 * <p/>
 * itemMaxAge is set to 0 to always hit the remote
 * <p/>
 * autoblock time is set to 3s for this test
 * <p/>
 * the test methods depend on the state of the repository left from the previous method.
 * (using testng's dependOnMethod here)
 *
 * @since 1.10.0
 */
public class Nexus4593NoAutoblockFor403IT
    extends AbstractNexusProxyIntegrationTest
{

    private Server server;

    @AfterMethod
    public void stopServer()
        throws Exception
    {
        if ( server != null )
        {
            server.stop();
        }
    }

    /**
     * Verify that a remote answering with '403 Forbidden' will not be auto-blocked
     * with and without a local artifact cached.
     */
    @Test
    public void testNoAutoblockOn403()
        throws Exception
    {
        startErrorServer( 403 );

        try
        {
            downloadArtifact( GavUtil.newGav( "g", "a", "v" ), "target" );
            assertThat( "should fail b/c of 403", false );
        }
        catch ( IOException e )
        {
            // expected, remote will answer with 403
        }

        assertThat( ProxyMode.valueOf( getStatus().getProxyMode() ), is( ProxyMode.ALLOW ) );

        // successfully fetch different artifact
        stopServer();
        this.proxyServer.start();
        downloadArtifact( GavUtil.newGav( "nexus4593", "artifact", "1.0.0" ), "target" );
        assertThat( ProxyMode.valueOf( getStatus().getProxyMode() ), is( ProxyMode.ALLOW ) );

        // 403 for artifact again
        startErrorServer( 403 );
        // download will not fail because we have a local copy cached, but the remote will be hit b/c maxAge is set to 0
        downloadArtifact( GavUtil.newGav( "nexus4593", "artifact", "1.0.0" ), "target" );

        assertThat( ProxyMode.valueOf( getStatus().getProxyMode() ), is( ProxyMode.ALLOW ) );
    }

    /**
     * Verify that a remote answering with '401' will still be auto-blocked.
     * <p/>
     * This test will change repo status to auto blocked.
     */
    @Test( dependsOnMethods = "testNoAutoblockOn403" )
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

    /**
     * Verify that un-autoblocking a repo works when 'HEAD /' is giving 403.
     */
    @Test( dependsOnMethods = "testAutoblockOn401" )
    public void testUnAutoblockFor403()
        throws Exception
    {
        startErrorServer( 403 );
        assertThat( ProxyMode.valueOf( getStatus().getProxyMode() ), is( ProxyMode.BLOCKED_AUTO ) );

        for ( int i = 0; i < 10; i++ )
        {
            if ( !ProxyMode.valueOf( getStatus().getProxyMode() ).equals( ProxyMode.BLOCKED_AUTO ) )
            {
                break;
            }
            Thread.sleep( 1000 );
        }

        assertThat( "No UnAutoblock in 10s", ProxyMode.valueOf( getStatus().getProxyMode() ), is( ProxyMode.ALLOW ) );
    }

    private void startErrorServer( final int code )
        throws Exception
    {
        stopServer();
        proxyServer.stop();

        server = Server.withPort( proxyPort ).serve( "/*" ).withBehaviours( error( code ) ).start();
    }

    private RepositoryStatusResource getStatus()
        throws IOException
    {
        return new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML ).getStatus(
            getTestRepositoryId() );
    }

    @BeforeClass
    public static void setAutoblockTime()
    {
        System.setProperty( "plexus.autoblock.remote.status.retain.time", String.valueOf( 3 * 1000 ) );
    }

    @AfterClass
    public static void restoreAutoblockTime()
    {
        System.clearProperty( "plexus.autoblock.remote.status.retain.time" );
    }
}

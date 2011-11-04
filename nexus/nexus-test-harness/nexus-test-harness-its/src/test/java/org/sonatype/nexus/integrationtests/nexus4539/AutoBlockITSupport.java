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
package org.sonatype.nexus.integrationtests.nexus4539;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.mortbay.jetty.servlet.ServletHolder;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.jettytestsuite.ControlledServer;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import com.google.common.collect.Lists;

/**
 * Support class for autoblock ITs
 */
public abstract class AutoBlockITSupport
    extends AbstractNexusIntegrationTest
{

    protected static final String REPO = "basic";

    protected ControlledServer server;

    protected Integer sleepTime;

    protected List<Object> pathsTouched;

    protected RepositoryMessageUtil repoUtil;

    public AutoBlockITSupport()
    {
        super( REPO );
    }

    @SuppressWarnings( "serial" )
    @BeforeMethod
    public void setup()
        throws Exception
    {
        sleepTime = -1;
        pathsTouched = Lists.newArrayList();

        server = lookup( ControlledServer.class );
        server.getProxyingContext().addServlet( new ServletHolder( new GenericServlet()
        {
            @Override
            public void service( ServletRequest req, ServletResponse res )
                throws ServletException, IOException
            {
                pathsTouched.add( ( (HttpServletRequest) req ).getPathInfo() );
                try
                {
                    if ( sleepTime != -1 )
                        Thread.sleep( sleepTime * 1000 );
                }
                catch ( InterruptedException e )
                {
                    HttpServletResponse resp = (HttpServletResponse) res;
                    resp.sendError( Status.CLIENT_ERROR_REQUEST_TIMEOUT.getCode() );
                }
            }
        } ), "/*" );
        server.start();

        this.repoUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @AfterMethod
    public void shutdown()
        throws StoppingException
    {
        pathsTouched = null;
        server.stop();
    }

    /**
     * Just request anything so if request is processed and error happens, it will auto block immediately
     */
    protected void shakeNexus()
        throws IOException
    {
        // don't wanna hit nexus NFC
        Gav gav = GavUtil.newGav( "nexus4539", "a", "404-" + System.nanoTime() );
        try
        {
            downloadArtifactFromRepository( REPO, gav, "target/downloads/nexus4539" );
        }
        catch ( FileNotFoundException e )
        {
            // ignore just fine
            // e.printStackTrace();
        }
    }

    /**
     * request status every half second until it matches expected status
     * 
     * @param mode
     */
    protected RepositoryStatusResource waitFor( RemoteStatus status, ProxyMode mode )
        throws Exception
    {
        RepositoryStatusResource s = null;
        for ( int i = 0; i < 1000; i++ )
        {
            s = this.repoUtil.getStatus( REPO );
            log.debug( "Waiting for: " + status + "," + mode + " - " + getJsonXStream().toXML( s ) );
            if ( status.name().equals( s.getRemoteStatus() ) && mode.name().equals( s.getProxyMode() ) )
            {
                return s;
            }
            Thread.sleep( 500 );
        }

        assertStatus( s, status, mode );

        throw new IllegalStateException();
    }

    /**
     * Assert status
     */
    protected void assertStatus( RepositoryStatusResource s, RemoteStatus status, ProxyMode mode )
    {
        assertThat( s, notNullValue() );
        assertThat( s.getRemoteStatus(), equalTo( status.toString() ) );
        assertThat( s.getProxyMode(), equalTo( mode.toString() ) );
    }

    @BeforeClass
    public static void fixAutoblockTime()
    {
        // NEXUS-4539 - to get test faster reduced autoblock check from 5 minutes to 30 seconds
        System.setProperty( "plexus.autoblock.remote.status.retain.time", String.valueOf( 30 * 1000 ) );
    }

    @AfterClass
    public static void restoreAutoblockTime()
    {
        System.clearProperty( "plexus.autoblock.remote.status.retain.time" );
    }

}
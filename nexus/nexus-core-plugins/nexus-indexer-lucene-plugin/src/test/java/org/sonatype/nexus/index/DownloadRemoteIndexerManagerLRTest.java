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
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.context.IndexingContext;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;

// This is an IT just because it runs longer then 15 seconds
public class DownloadRemoteIndexerManagerLRTest
    extends AbstractIndexerManagerTest
{
    private Server server;

    private File fakeCentral;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        fakeCentral = new File( getBasedir(), "target/repos/fake-central" );
        fakeCentral.mkdirs();

        // create proxy server
        ServerSocket s = new ServerSocket( 0 );
        int port = s.getLocalPort();
        s.close();

        server = new Server( port );

        ResourceHandler resource_handler = new ResourceHandler()
        {
            @Override
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                System.out.print( "JETTY: " + target );
                super.handle( target, request, response, dispatch );
                System.out.println( "  ::  " + ((Response)response).getStatus() );
            }
        };
        resource_handler.setResourceBase( fakeCentral.getAbsolutePath() );
        HandlerList handlers = new HandlerList();
        handlers.setHandlers( new Handler[] { resource_handler, new DefaultHandler() } );
        server.setHandler( handlers );

        System.out.print( "JETTY Started on port: " + port );
        server.start();

        // update central to use proxy server
        central.setDownloadRemoteIndexes( true );
        central.setRemoteUrl( "http://localhost:" + port );
        central.setRepositoryPolicy( RepositoryPolicy.SNAPSHOT );

        nexusConfiguration.saveConfiguration();

        Thread.sleep( 100 );

        wairForAsyncEventsToCalmDown();
        waitForTasksToStop();
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        server.stop();

        FileUtils.forceDelete( fakeCentral );

        super.tearDown();
    }

    @Test
    public void testRepoReindex()
        throws Exception
    {
        File index1 = new File( getBasedir(), "src/test/resources/repo-index/index" );
        File index2 = new File( getBasedir(), "src/test/resources/repo-index/index2" );
        File centralIndex = new File( fakeCentral, ".index" );

        // copy index 02
        overwriteIndex( index2, centralIndex );

        super.indexerManager.reindexRepository( null, central.getId(), true );

        searchFor( "org.sonatype.nexus", 8, central.getId() );

        // copy index 01
        overwriteIndex( index1, centralIndex );

        super.indexerManager.reindexRepository( null, central.getId(), true );

        searchFor( "org.sonatype.nexus", 1, central.getId() );

        // copy index 02
        overwriteIndex( index2, centralIndex );

        super.indexerManager.reindexRepository( null, central.getId(), true );

        searchFor( "org.sonatype.nexus", 8, central.getId() );
    }

    private void overwriteIndex( File source, File destination )
        throws Exception
    {
        File indexFile = new File( destination, "nexus-maven-repository-index.gz" );
        File indexProperties = new File( destination, "nexus-maven-repository-index.properties" );

        long lastMod = -1;
        if ( destination.exists() )
        {
            FileUtils.forceDelete( destination );
            lastMod = indexFile.lastModified();
        }
        FileUtils.copyDirectory( source, destination, false );
        long lastMod2 = indexFile.lastModified();
        assertTrue( lastMod < lastMod2 );

        Properties p = new Properties();
        InputStream input = new FileInputStream( indexProperties );
        p.load( input );
        input.close();

        p.setProperty( "nexus.index.time", format( new Date() ) );
        p.setProperty( "nexus.index.timestamp", format( new Date() ) );

        OutputStream output = new FileOutputStream( indexProperties );
        p.store( output, null );
        output.close();
    }

    private String format( Date d )
    {
        SimpleDateFormat df = new SimpleDateFormat( IndexingContext.INDEX_TIME_FORMAT );
        df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return df.format( d );
    }
}

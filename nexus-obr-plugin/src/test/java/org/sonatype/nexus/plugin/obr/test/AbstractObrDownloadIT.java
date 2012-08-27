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
package org.sonatype.nexus.plugin.obr.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;

public abstract class AbstractObrDownloadIT
    extends AbstractOBRIntegrationTest
{
    protected static final File FELIX_HOME = new File( "target/felix/org.apache.felix.main.distribution-3.2.2" );

    protected static final File FELIX_REPO = new File( "target/felix-repo" );

    protected static final File FELIX_CONF = new File( "target/test-classes/config.properties" );

    public AbstractObrDownloadIT()
    {
        super();
    }

    protected void downloadApacheFelixWebManagement( final String repoId )
        throws IOException, Exception, MalformedURLException
    {
        FileUtils.deleteDirectory( new File( FELIX_HOME, "felix-cache" ) );
        FileUtils.deleteDirectory( new File( FELIX_REPO, ".meta" ) );

        downloadFile( new URL( getRepositoryUrl( repoId ) + ".meta/obr.xml" ), "target/" + getTestId()
            + "/download/obr.xml" );

        final ProcessBuilder pb =
            new ProcessBuilder( "java", "-Dfelix.config.properties=" + FELIX_CONF.toURI(), "-jar", "bin/felix.jar" );

        pb.directory( FELIX_HOME );
        pb.redirectErrorStream( true );
        final Process p = pb.start();

        final Object lock = new Object();

        final Thread t = new Thread( new Runnable()
        {
            public void run()
            {
                // just a safeguard, if felix get stuck kill everything
                try
                {
                    synchronized ( lock )
                    {
                        lock.wait( 5 * 1000 * 60 );
                    }
                }
                catch ( final InterruptedException e )
                {
                    // ignore
                }
                p.destroy();
            }
        } );
        t.setDaemon( true );
        t.start();

        synchronized ( lock )
        {
            final InputStream input = p.getInputStream();
            final OutputStream output = p.getOutputStream();
            waitFor( input, "g!" );
            output.write( ( "obr:repos add " + getRepositoryUrl( repoId ) + ".meta/obr.xml\r\n" ).getBytes() );
            output.flush();
            waitFor( input, "g!" );
            output.write( "obr:deploy -s org.apache.felix.webconsole\r\n".getBytes() );
            output.flush();
            waitFor( input, "done." );
            p.destroy();

            lock.notifyAll();
        }
    }

    private void waitFor( final InputStream input, final String expectedLine )
        throws Exception
    {
        final long startMillis = System.currentTimeMillis();
        final StringBuilder content = new StringBuilder();
        do
        {
            final int available = input.available();
            if ( available > 0 )
            {
                final byte[] bytes = new byte[available];
                input.read( bytes );
                final String current = new String( bytes );
                System.out.print( current );
                content.append( current );
                Thread.yield();
            }
            else if ( System.currentTimeMillis() - startMillis > 5 * 60 * 1000 )
            {
                throw new InterruptedException(); // waited for more than 5 minutes
            }
            else
            {
                try
                {
                    Thread.sleep( 100 );
                }
                catch ( final InterruptedException e )
                {
                    // continue...
                }
            }
        }
        while ( content.indexOf( expectedLine ) == -1 );
    }

}

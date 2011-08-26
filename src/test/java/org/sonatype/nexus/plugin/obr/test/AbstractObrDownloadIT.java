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
package org.sonatype.nexus.plugin.obr.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractObrDownloadIT
    extends AbstractOBRIntegrationTest
{
    protected static final File FELIX_HOME = new File( "target/felix/org.apache.felix.main.distribution-3.2.2" );

    protected static final File FELIX_REPO = new File( "target/felix-repo" );

    public AbstractObrDownloadIT()
    {
        super();
    }

    protected void downloadApacheFelixWebManagement( final String repoId )
        throws IOException, Exception, MalformedURLException
    {
        FileUtils.deleteDirectory( new File( FELIX_HOME, "felix-cache" ) );

        final ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( repoId );

        TaskScheduleUtil.runTask( "PublishObrDescriptorTask", prop );

        downloadFile( new URL( getRepositoryUrl( repoId ) + ".meta/obr.xml" ), "target/" + getTestId()
            + "/download/obr.xml" );

        final ProcessBuilder pb = new ProcessBuilder( "java", "-jar", "bin/felix.jar" );
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
            output.write( ( "obr:repos remove http://felix.apache.org/obr/releases.xml\r\n" ).getBytes() );
            output.flush();
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
        final StringBuilder content = new StringBuilder();
        do
        {
            final byte[] bytes = new byte[input.available()];
            input.read( bytes );
            final String current = new String( bytes );
            System.out.print( current );
            content.append( current );
            Thread.yield();
        }
        while ( content.indexOf( expectedLine ) == -1 );
    }

}

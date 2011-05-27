/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
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
    protected static final File FELIX_HOME = new File( "target/felix/org.apache.felix.main.distribution-2.0.5" );

    protected static final File FELIX_REPO = new File( "target/felix-repo" );

    public AbstractObrDownloadIT()
    {
        super();
    }

    protected void downloadApacheFelixWebManagement( String repoId )
        throws IOException, Exception, MalformedURLException
    {
        FileUtils.deleteDirectory( new File( FELIX_HOME, "felix-cache" ) );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey( "repositoryId" );
        prop.setValue( repoId );

        TaskScheduleUtil.runTask( "PublishObrDescriptorTask", prop );
        
        downloadFile( new URL( getRepositoryUrl( repoId ) + ".meta/obr.xml" ), "target/" + getTestId()
            + "/download/obr.xml" );

        ProcessBuilder pb = new ProcessBuilder( "java", "-jar", "bin/felix.jar" );
        pb.directory( FELIX_HOME );
        pb.redirectErrorStream( true );
        final Process p = pb.start();

        final Object lock = new Object();

        Thread t = new Thread( new Runnable()
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
                catch ( InterruptedException e )
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
            InputStream input = p.getInputStream();
            OutputStream output = p.getOutputStream();
            waitFor( input, "->" );
            output.write( ( "obr add-url " + getRepositoryUrl( repoId ) + ".meta/obr.xml\r\n" ).getBytes() );
            output.flush();
            waitFor( input, "->" );
            output.write( "obr start 'Apache Felix Web Management Console'\r\n".getBytes() );
            output.flush();
            waitFor( input, "Deploying...done." );
            p.destroy();

            lock.notifyAll();
        }
    }

    private void waitFor( InputStream input, String expectedLine )
        throws Exception
    {
        StringBuilder content = new StringBuilder();
        do
        {
            byte[] bytes = new byte[input.available()];
            input.read( bytes );
            String current = new String( bytes );
            System.out.print( current );
            content.append( current );
            Thread.yield();
        }
        while ( content.indexOf( expectedLine ) == -1 );
    }

}
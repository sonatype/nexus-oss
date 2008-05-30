/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.
 *
 * This file is part of Nexus.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.test;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.DefaultArchiverManager;
import org.sonatype.appbooter.PlexusContainerHost;
import org.sonatype.appbooter.ctl.ControlConnectionException;
import org.sonatype.appbooter.ctl.ControllerClient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public abstract class AbstractNexusTest extends PlexusTestCase
{
    private String nexusUrl;
    private static final int TEST_CONNECTION_ATTEMPTS = 5;
    private static final int TEST_CONNECTION_TIMEOUT = 3000;

    public AbstractNexusTest( String nexusUrl )
    {
        this.nexusUrl = nexusUrl;
    }

    private boolean testConnection( int attempts, int timeout )
    {
        if ( attempts < 1 )
        {
            throw new IllegalArgumentException( "Must have at least 1 attempt" );
        }

        if ( timeout < 1 )
        {
            throw new IllegalArgumentException( "Must have at least 1 millisecond timeout" );
        }

        boolean result = false;

        for ( int i = 0; i < attempts; i++)
        {
            try
            {
                URL url = new URL( nexusUrl );
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout( timeout );
                InputStream stream = connection.getInputStream();
                stream.close();
                result = true;
                break;
            }
            catch ( IOException e )
            {
                //Just break out to skip the unnecessary sleep
                if ( ( i + 1 ) == attempts )
                {
                    break;
                }
                try
                {
                    Thread.sleep( timeout );
                }
                catch ( InterruptedException e1 )
                {
                }
            }
        }


        return result;
    }

    protected void stopNexus()
    {
        ControllerClient client = null;
        try
        {
            client = new ControllerClient( PlexusContainerHost.DEFAULT_CONTROL_PORT );
            client.stop();

            //Note calling testConnection w/ only 1 attempt, becuase just 1 timeout will do
            assertFalse( testConnection( 1, TEST_CONNECTION_TIMEOUT ) );
        }
        catch ( UnknownHostException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( ControlConnectionException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            assert( false );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }

    protected void startNexus()
    {
        ControllerClient client = null;
        try
        {
            client = new ControllerClient( PlexusContainerHost.DEFAULT_CONTROL_PORT );
            client.start();

            assertTrue( testConnection( TEST_CONNECTION_ATTEMPTS, TEST_CONNECTION_TIMEOUT ) );
        }
        catch ( UnknownHostException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( ControlConnectionException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            assert( false );
        }
        finally
        {
            if ( client != null )
            {
                client.close();
            }
        }
    }

    protected void restartNexus()
    {
        stopNexus();
        startNexus();
    }

    protected File downloadArtifact( String groupId, String artifact, String version, String type, String targetDirectory )
    {
        URL url = null;
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;

        new File( targetDirectory ).mkdirs();

        File downloadedFile = new File( targetDirectory + "/" + artifact + "-" + version + "." + type );
        try
        {
            url = new URL( nexusUrl + groupId.replace( '.', '/' ) + "/" + artifact + "/" + version + "/" + artifact + "-" + version + "." + type);
            out = new BufferedOutputStream( new FileOutputStream( downloadedFile ) );
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ( ( numRead = in.read( buffer ) ) != -1 )
            {
                out.write( buffer, 0, numRead );
                numWritten += numRead;
            }
        }
        catch ( MalformedURLException e )
        {
            e.printStackTrace();
            assert( false );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
            assert( false );
        }
        finally
        {
            try
            {
                if ( out != null )
                {
                    out.close();
                }
                if ( in != null )
                {
                    in.close();
                }
            }
            catch ( IOException e )
            {
            }

        }

        return downloadedFile;
    }

    protected File unpackArtifact( File artifact, String targetDirectory )
    {
        File target = null;
        try
        {
            target = new File( targetDirectory );
            target.mkdirs();
            ArchiverManager manager = (ArchiverManager) lookup( DefaultArchiverManager.ROLE );
            UnArchiver unarchiver = manager.getUnArchiver( artifact );
            unarchiver.setSourceFile( artifact );
            unarchiver.setDestDirectory( target );
            unarchiver.extract();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            assert( false );
        }

        return target;
    }
}

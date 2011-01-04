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
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.test.launcher.ThreadedPlexusAppBooterService;
import org.testng.Assert;

import com.thoughtworks.xstream.XStream;

/**
 * Simple util class
 */
public class NexusStatusUtil
{
    protected static Logger log = Logger.getLogger( NexusStatusUtil.class );

    private ThreadedPlexusAppBooterService APP_BOOTER_SERVICE = null;

    public StatusResourceResponse getNexusStatus()
        throws NexusIllegalStateException
    {
        Response response;
        try
        {
            response = RequestFacade.doGetRequest( "service/local/status" );
        }
        catch ( IOException e )
        {
            throw new NexusIllegalStateException( "Unable to retrieve nexus status", e );
        }

        if ( !response.getStatus().isSuccess() )
        {
            throw new NexusIllegalStateException( "Error retrieving current status " + response.getStatus().toString() );
        }

        XStream xstream = XStreamFactory.getXmlXStream();

        String entityText;
        try
        {
            entityText = response.getEntity().getText();
        }
        catch ( IOException e )
        {
            throw new NexusIllegalStateException( "Unable to retrieve nexus status " + new XStream().toXML( response ),
                                                  e );
        }

        StatusResourceResponse status = (StatusResourceResponse) xstream.fromXML( entityText );

        return status;
    }

    public void start( String testId )
        throws Exception
    {
        int totalWaitCycles = 200 * 5; // 200 sec
        int retryStartCycles = 50 * 5; // 50 sec
        int pollingFreq = 200; // 200 ms

        log.info( "wait for Nexus start" );
        for ( int i = 0; i < totalWaitCycles; i++ )
        {

            if ( i % retryStartCycles == 0 )
            {
                getAppBooterService( testId ).start();
            }

            if ( isNexusRunning() )
            {
                // nexus started
                return;
            }

            try
            {
                Thread.sleep( pollingFreq );
            }
            catch ( InterruptedException e )
            {
                // no problem
            }
        }

        try
        {
            getAppBooterService( testId ).shutdown();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        throw new NexusIllegalStateException( "Unable to doHardStart(), nexus still stopped, took 200s" );

    }

    public void stop()
        throws Exception
    {
        if ( APP_BOOTER_SERVICE == null )
        {
            // app booter wasn't started, won't do it on stop
            return;
        }

        final ThreadedPlexusAppBooterService appBooterService = APP_BOOTER_SERVICE;

        try
        {
            try
            {
                appBooterService.stop();
            }
            catch ( Exception e )
            {
                System.err.println( "Failed to stop Nexus. The thread will most likely die with an error: "
                    + e.getMessage() );
                Assert.fail( e.getMessage() );
            }
            finally
            {
            }

            if ( !waitForStop() )
            {
                // just start over if we can't stop normally
                System.out.println( "Forcing Stop of appbooter" );
                appBooterService.forceStop();
                APP_BOOTER_SERVICE = null;
            }
        }
        finally
        {
            appBooterService.clean();
        }
    }

    public boolean isNexusRunning()
    {
        Socket sock = null;
        try
        {
            sock = new Socket("localhost", AbstractNexusIntegrationTest.nexusApplicationPort);
        }
        catch ( UnknownHostException e1 )
        {
            log.debug( "nexus application port isn't open." );
            return false;
        }
        catch ( IOException e1 )
        {
            log.debug( "nexus application port isn't open." );
            return false;
        }
        finally
        {
            if ( sock != null )
            {
                try
                {
                    sock.close();
                }
                catch ( IOException e )
                {
                }
            }
        }

        try
        {
            getNexusStatus();
            log.debug( "nexus is running." );
            return true;
        }
        catch ( NexusIllegalStateException e )
        {
            log.debug( "nexus application port is open, but not yet responding to requests." );
            return false;
        }

    }

    public boolean isNexusStopped()
        throws NexusIllegalStateException
    {
        return !isNexusRunning();
    }

    public boolean waitForStop()
        throws NexusIllegalStateException
    {
        log.info( "wait for Nexus stop" );

        int totalWaitTime = 40 * 1000; // 20 sec
        int pollingFreq = 200; // 200 ms

        for ( int i = 0; i < totalWaitTime / pollingFreq; i++ )
        {
            log.debug( "wait for Nexus stop, attempt: " + i );
            if ( !isNexusRunning() )
            {
                // nexus stopped!
                return true;
            }

            try
            {
                Thread.sleep( pollingFreq );
            }
            catch ( InterruptedException e )
            {
                // no problem
            }
        }

        // didn't stopped!
        return false;
    }

    private ThreadedPlexusAppBooterService getAppBooterService( String testId )
        throws Exception
    {
        if ( APP_BOOTER_SERVICE == null )
        {
            final File f = new File( "target/plexus-home" );

            if ( !f.isDirectory() )
            {
                f.mkdirs();
            }

            File bundleRoot = new File( TestProperties.getAll().get( "nexus.base.dir" ) );
            System.setProperty( "basedir", bundleRoot.getAbsolutePath() );

            // System.setProperty( "plexus.appbooter.customizers", "org.sonatype.nexus.NexusBooterCustomizer,"
            // + ITAppBooterCustomizer.class.getName() );

            File classworldsConf = new File( bundleRoot, "conf/classworlds.conf" );

            if ( !classworldsConf.isFile() )
            {
                throw new IllegalStateException( "The bundle classworlds.conf file is not found (\""
                    + classworldsConf.getAbsolutePath() + "\")!" );
            }

            System.setProperty( "classworlds.conf", classworldsConf.getAbsolutePath() );

            // this is non trivial here, since we are running Nexus in _same_ JVM as tests
            // and the PlexusAppBooterJSWListener (actually theused WrapperManager in it) enforces then Nexus may be
            // started only once in same JVM!
            // So, we are _overrriding_ the in-bundle plexus app booter with the simplest one
            // since we dont need all the bells-and-whistles in Service and JSW
            // but we are still _reusing_ the whole bundle environment by tricking Classworlds Launcher

            ServerSocket socket = new ServerSocket( 0 );
            int controlPort = socket.getLocalPort();
            socket.close();

            APP_BOOTER_SERVICE = new ThreadedPlexusAppBooterService( classworldsConf, controlPort, testId );
        }

        return APP_BOOTER_SERVICE;
    }

}

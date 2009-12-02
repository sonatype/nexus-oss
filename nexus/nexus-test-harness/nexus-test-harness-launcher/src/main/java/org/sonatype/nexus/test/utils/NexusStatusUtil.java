/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.StatusResourceResponse;
import org.sonatype.nexus.test.launcher.ThreadedPlexusAppBooterService;

import com.thoughtworks.xstream.XStream;

/**
 * Simple util class
 */
public class NexusStatusUtil
{

    protected static Logger log = Logger.getLogger( NexusStatusUtil.class );

    private static final String STATUS_STARTED = "STARTED";

    private static ThreadedPlexusAppBooterService APP_BOOTER_SERVICE = null;

    public static StatusResourceResponse getNexusStatus()
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

    @Deprecated
    public static void doSoftStart()
        throws Exception
    {
        start();
    }

    @Deprecated
    public static void doSoftStop()
        throws Exception
    {
        stop();
    }

    @Deprecated
    public static void doHardStart()
        throws Exception
    {
        start();
    }

    public static void start()
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
                getAppBooterService().start();
            }

            // log.debug( "wait for Nexus start, attempt: " + i );
            try
            {
                if ( isNexusRunning() )
                {
                    // nexus started
                    return;
                }
            }
            catch ( NexusIllegalStateException e )
            {
                // let's give it more time
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
            getAppBooterService().shutdown();
        }
        catch ( Throwable t )
        {
            t.printStackTrace();
        }
        throw new NexusIllegalStateException( "Unable to doHardStart(), nexus still stopped, took 200s" );

    }

    @Deprecated
    public static void doHardStop()
        throws Exception
    {
        stop();
    }

    @Deprecated
    public static void doHardStop( boolean checkStarted )
        throws Exception
    {
        stop();
    }

    public static void stop()
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
            // NOTE: We can't kill active tasks, we need to wait for them to stop
            TaskScheduleUtil.waitForAllTasksToStop();

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
                APP_BOOTER_SERVICE = null;
            }

            if ( !waitForStop() )
            {
                // just start over if we can't stop normally
                System.out.println( "Forcing Stop of appbooter" );
                appBooterService.forceStop();
            }
        }
        finally
        {
            appBooterService.clean();
        }
    }

    public static boolean isNexusAlive()
    {
        return isNexusAlive( new int[] { AbstractNexusIntegrationTest.nexusControlPort,
            AbstractNexusIntegrationTest.nexusApplicationPort } );
    }

    public static boolean isNexusControllerPortAlive()
    {
        return isNexusAlive( new int[] { AbstractNexusIntegrationTest.nexusControlPort } );
    }

    public static boolean isNexusAlive( int[] ports )
    {
        ServerSocket ss = null;
        for ( int i = 0; i < ports.length; i++ )
        {
            int port = ports[i];
            try
            {
                ss = new ServerSocket( port );
            }
            catch ( IOException e )
            {
                // ok, port in use, means nexus is active
                return true;
            }
            finally
            {
                try
                {
                    if ( ss != null )
                    {
                        ss.close();
                    }
                }
                catch ( IOException e )
                {
                    // just closing
                }
            }
        }

        return false;
    }

    public static boolean isNexusRunning()
        throws NexusIllegalStateException
    {
        return isNexusAlive() && ( STATUS_STARTED.equals( getNexusStatus().getData().getState() ) );
    }

    public static boolean isNexusStopped()
        throws NexusIllegalStateException
    {
        return !isNexusAlive();
    }

    public static boolean waitForStop()
        throws NexusIllegalStateException
    {
        log.info( "wait for Nexus stop" );

        int totalWaitTime = 40 * 1000; // 20 sec
        int pollingFreq = 200; // 200 ms

        for ( int i = 0; i < totalWaitTime / pollingFreq; i++ )
        {
            log.debug( "wait for Nexus stop, attempt: " + i );
            if ( !isNexusAlive() )
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

    private static ThreadedPlexusAppBooterService getAppBooterService()
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

            APP_BOOTER_SERVICE = new ThreadedPlexusAppBooterService( classworldsConf, controlPort );
        }
        return APP_BOOTER_SERVICE;
    }

}

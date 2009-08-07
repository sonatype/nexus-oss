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

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.log4j.Logger;
import org.codehaus.plexus.PlexusContainerException;
import org.restlet.data.Response;
import org.sonatype.appbooter.PlexusAppBooter;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

import com.thoughtworks.xstream.XStream;

/**
 * Simple util class
 */
public class NexusStatusUtil
{

    protected static Logger log = Logger.getLogger( NexusStatusUtil.class );

    private static final String STATUS_STOPPED = "STOPPED";

    private static final String STATUS_STARTED = "STARTED";

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
        throws NexusIllegalStateException, PlexusContainerException
    {
        start();
    }

    @Deprecated
    public static void doSoftStop()
        throws NexusIllegalStateException
    {
        stop();
    }

    @Deprecated
    public static void doHardStart()
        throws NexusIllegalStateException, PlexusContainerException
    {
        start();
    }

    public static void start()
        throws NexusIllegalStateException, PlexusContainerException
    {
        PlexusAppBooter appBooter = TestContainer.getInstance().getPlexusAppBooter();
        if ( appBooter.isStarted() )
        {
            appBooter.stopContainer();
        }

        appBooter.startContainer();

        if ( !waitForStart() )
        {
            throw new NexusIllegalStateException( "Unable to doHardStart(), nexus still stopped" );
        }

        isNexusRunning();
    }

    @Deprecated
    public static void doHardStop()
        throws NexusIllegalStateException
    {
        stop();
    }

    @Deprecated
    public static void doHardStop( boolean checkStarted )
        throws NexusIllegalStateException
    {
        stop();
    }

    public static void stop()
        throws NexusIllegalStateException
    {
        PlexusAppBooter appBooter = TestContainer.getInstance().getPlexusAppBooter();
        if ( appBooter.isStarted() )
        {
            appBooter.stopContainer();

            if ( !waitForStop() )
            {
                throw new NexusIllegalStateException( "Unable to doHardStop(), nexus still running" );
            }
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
        return !isNexusAlive() || ( STATUS_STOPPED.equals( getNexusStatus().getData().getState() ) );
    }

    public static boolean waitForStart()
        throws NexusIllegalStateException
    {
        log.info( "wait for Nexus start" );
        for ( int i = 0; i < 20; i++ )
        {
            log.debug( "wait for Nexus start, attempt: " + i );
            try
            {
                if ( isNexusRunning() )
                {
                    // nexus started
                    return true;
                }
            }
            catch ( NexusIllegalStateException e )
            {
                // let's give it more time
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                // no problem
            }
        }

        // Didn't start
        return false;
    }

    public static boolean waitForStop()
        throws NexusIllegalStateException
    {
        log.info( "wait for Nexus stop" );

        for ( int i = 0; i < 20; i++ )
        {
            log.debug( "wait for Nexus stop, attempt: " + i );
            if ( !isNexusAlive() )
            {
                // nexus stopped!
                return true;
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                // no problem
            }

            if ( isNexusAlive() && !isNexusRunning() )
            {
                throw new NexusIllegalStateException( "Nexus is no longer running, but still using HTTP ports!" );
            }
        }

        // didn't stopped!
        return false;
    }

}

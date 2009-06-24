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
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.sonatype.appbooter.AbstractForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.rest.NexusRestClient;
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

    private static NexusClient client;

    public static void sendNexusStatusCommand( String command )
        throws NexusIllegalStateException
    {
        String originalState = getNexusStatus().getData().getState();

        Response response;
        try
        {
            response =
                RequestFacade.sendMessage( "service/local/status/command", Method.PUT,
                                           new StringRepresentation( command, MediaType.TEXT_ALL ) );
        }
        catch ( IOException e )
        {
            throw new NexusIllegalStateException( "Unable to retrieve nexus status", e );
        }

        if ( !response.getStatus().isSuccess() )
        {
            try
            {
                throw new NexusIllegalStateException( "Could not " + command + " Nexus: (" + response.getStatus() + ")"
                    + ((response.getEntity() != null)  ? response.getEntity().getText() : "no response") + " nexus state was: " + originalState + " and now is: "
                    + getNexusStatus().getData().getState() );
            }
            catch ( IOException e )
            {
                throw new NexusIllegalStateException( "Could not " + command + " Nexus: (" + response.getStatus() + ")"
                    + new XStream().toXML( response ) + " nexus state was: " + originalState + " and now is: "
                    + getNexusStatus().getData().getState(), e );
            }
        }
    }

    private static ForkedAppBooter getAppBooter()
        throws Exception
    {
        if ( System.getProperty( "classpath.conf" ) == null )
        {
            return (AbstractForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE,
                                                                                 "TestForkedAppBooter" );
        }
        else
        {
            return (UnforkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE,
                                                                           "TestUnforkedAppBooter" );
        }
    }

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

    public static void doClientStart()
        throws NexusIllegalStateException
    {
        if ( !isNexusAlive() )
        {
            throw new NexusIllegalStateException( "Unable to doClientStart(), nexus is not running" );
        }

        try
        {
            getClient().startNexus();
        }
        catch ( Exception e )
        {
            throw new NexusIllegalStateException( "Unable to doClientStart(), nexus still stopped", e );
        }

        if ( !waitForStart() )
        {
            throw new NexusIllegalStateException( "Unable to doClientStart(), nexus still stopped" );
        }
    }

    public static void doClientStop()
        throws NexusIllegalStateException
    {
        if ( !isNexusAlive() )
        {
            throw new NexusIllegalStateException( "Unable to doClientStop(), nexus is not running" );
        }

        if ( isNexusStopped() )
        {
            log.warn( "Nexus is already stopped" );

        }
        else
        {

            try
            {
                getClient().stopNexus();
            }
            catch ( Exception e )
            {
                throw new NexusIllegalStateException( "Unable to doClientStop(), nexus still running", e );
            }

            if ( !waitForStop() )
            {
                throw new NexusIllegalStateException( "Unable to doClientStop(), nexus still running" );
            }

            disconnect();
        }
    }

    public static void doHardStart()
        throws NexusIllegalStateException
    {
        if ( isNexusAlive() )
        {
            log.warn( "Nexus is already started" );
            if ( !isNexusRunning() )
            {
                throw new NexusIllegalStateException( "Nexus is no longer active, but still using HTTP ports" );
            }
        }
        else
        {
            try
            {
                ForkedAppBooter appBooter = getAppBooter();
                if ( appBooter instanceof AbstractForkedAppBooter )
                {
                    ( (AbstractForkedAppBooter) appBooter ).setSleepAfterStart( 0 );
                }

                appBooter.start();
            }
            catch ( Exception e )
            {
                throw new NexusIllegalStateException( "Unable to doHardStart(), nexus still stopped", e );
            }

        }

        if ( !waitForStart() )
        {
            throw new NexusIllegalStateException( "Unable to doHardStart(), nexus still stopped" );
        }
    }

    public static void doHardStop()
        throws NexusIllegalStateException
    {
        doHardStop( true );
    }

    public static void doHardStop( boolean checkStarted )
        throws NexusIllegalStateException
    {
        if ( !isNexusAlive() )
        {
            log.warn( "Nexus is already stopped" );
        }
        else
        {
            try
            {
                getAppBooter().stop();
            }
            catch ( Exception e )
            {
                killNexus();
            }
        }

        if ( !waitForStop() )
        {
            killNexus();
            if ( !waitForStop() )
            {
                throw new NexusIllegalStateException( "Unable to doHardStop(), nexus still running" );
            }
        }

        if ( isNexusAlive() )
        {
            throw new NexusIllegalStateException( "Unable to doHardStop(), still using HTTP ports" );
        }

        disconnect();
    }

    public static NexusClient getClient()
        throws NexusIllegalStateException
    {
        if ( client == null )
        {
            client = new NexusRestClient();
            // at this point security should not be turned on, but you never know...
            try
            {
                client.connect( AbstractNexusIntegrationTest.nexusBaseUrl,
                                TestContainer.getInstance().getTestContext().getAdminUsername(),
                                TestContainer.getInstance().getTestContext().getAdminPassword() );
            }
            catch ( Exception e )
            {
                throw new NexusIllegalStateException( "Unable to start nexus client", e );
            }
        }
        return client;
    }

    public static boolean isNexusAlive()
    {
        int[] ports =
            new int[] { AbstractNexusIntegrationTest.nexusControlPort,
                AbstractNexusIntegrationTest.nexusApplicationPort };

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
        for ( int i = 0; i < 800; i++ )
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

        for ( int i = 0; i < 800; i++ )
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

    private static void killNexus()
        throws NexusIllegalStateException
    {
        disconnect();

        sendNexusStatusCommand( "KILL" );

        if ( isNexusAlive() )
        {
            throw new NexusIllegalStateException( "Unable to kill nexus" );
        }
    }

    private static void disconnect()
    {
        if ( client == null )
        {
            return;
        }

        try
        {
            client.disconnect();
        }
        catch ( Exception e )
        {
            // just stopping
        }

        client = null;
    }

}

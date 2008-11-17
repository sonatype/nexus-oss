package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.net.ConnectException;

import junit.framework.Assert;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;
import org.sonatype.appbooter.AbstractForkedAppBooter;
import org.sonatype.appbooter.ForkedAppBooter;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

import com.thoughtworks.xstream.XStream;

/**
 * Simple util class
 */
public class NexusStateUtil
{

    private static final String STATUS_STOPPED = "STOPPED";

    private static final String STATUS_STARTED = "STARTED";

    private static NexusClient client;

    public static void sendNexusStatusCommand( String command )
        throws IOException
    {

        Response response =
            RequestFacade.sendMessage( "service/local/status/command", Method.PUT,
                                       new StringRepresentation( command, MediaType.TEXT_ALL ) );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not " + command + " Nexus: (" + response.getStatus() + ")" );
        }
    }
    
    private static AbstractForkedAppBooter getAppBooter() throws Exception
    {
        return (AbstractForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
    }

    public static StatusResourceResponse getNexusStatus()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/status" );

        if ( !response.getStatus().isSuccess() )
        {
            throw new ConnectException( response.getStatus().toString() );
        }

        XStream xstream = XStreamFactory.getXmlXStream();

        String entityText = response.getEntity().getText();
        Assert.assertNotNull( "Invalid server response: " + new XStream().toXML( response ), entityText );

        StatusResourceResponse status = (StatusResourceResponse) xstream.fromXML( entityText );
        return status;
    }

    public static void doSoftStop()
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", getClient().isNexusStarted( true ) );

        sendNexusStatusCommand( "STOP" );

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", NexusStatusUtil.waitForStop( getClient() ) );

        getClient().disconnect();
        client = null;
    }

    public static void doSoftStart()
        throws Exception
    {
        Assert.assertFalse( "Nexus is already started.", getClient().isNexusStarted( true ) );

        sendNexusStatusCommand( "START" );

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", NexusStatusUtil.waitForStart( getClient() ) );
    }

    public static void doSoftRestart()
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", getClient().isNexusStarted( true ) );

        sendNexusStatusCommand( "RESTART" );

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", NexusStatusUtil.waitForStart( getClient() ) );
    }

    public static void doClientStart()
        throws Exception
    {
        Assert.assertFalse( "Nexus is already started.", getClient().isNexusStarted( true ) );

        getClient().startNexus();

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", NexusStatusUtil.waitForStart( getClient() ) );
    }

    public static void doClientStop()
        throws Exception
    {
        Assert.assertTrue( "Nexus is not started.", getClient().isNexusStarted( true ) );

        getClient().stopNexus();

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", NexusStatusUtil.waitForStop( getClient() ) );

        getClient().disconnect();
        client = null;
    }

    public static ForkedAppBooter doHardStart()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = getAppBooter();

        Assert.assertFalse( "Nexus is already started.", getClient().isNexusStarted( true ) );

        appBooter.setSleepAfterStart( 0 );
        appBooter.start();

        Assert.assertTrue( "Unable to start Nexus after 4 minutes", NexusStatusUtil.waitForStart( getClient() ) );

        return appBooter;
    }

    public static void doHardStop()
        throws Exception
    {
        AbstractForkedAppBooter appBooter = getAppBooter();
        doHardStop( true );
    }

    public static void doHardStop( boolean checkStarted )
        throws Exception
    {
        if ( checkStarted )
        {
            Assert.assertTrue( "Nexus is not started.", getClient().isNexusStarted( true ) );
        }

        getAppBooter().stop();

        Assert.assertTrue( "Unable to stop Nexus after 4 minutes", NexusStatusUtil.waitForStop( getClient() ) );

        getClient().disconnect();
        client = null;

    }

    public static NexusClient getClient()
        throws NexusClientException, NexusConnectionException
    {
        if ( client == null )
        {
            client = new NexusRestClient();
            // at this point security should not be turned on, but you never know...
            client.connect( TestProperties.getString( "nexus.base.url" ),
                            TestContainer.getInstance().getTestContext().getAdminUsername(),
                            TestContainer.getInstance().getTestContext().getAdminPassword() );
        }
        return client;
    }

    public static boolean isNexusRunning()
        throws IOException
    {
        return ( STATUS_STARTED.equals( getNexusStatus().getData().getState() ) );
    }

    public static boolean isNexusStopped()
        throws IOException
    {
        return ( STATUS_STOPPED.equals( getNexusStatus().getData().getState() ) );
    }

}

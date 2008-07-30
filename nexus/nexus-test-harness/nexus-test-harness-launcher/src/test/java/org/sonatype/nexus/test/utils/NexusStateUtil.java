package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.net.URL;

import junit.framework.Assert;

import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.rest.model.StatusResourceResponse;

import com.thoughtworks.xstream.XStream;


/**
 * Simple util class
 *
 */
public class NexusStateUtil
{
    
    private static final String STATUS_STOPPED = "STOPPED";

    private static final String STATUS_STARTED = "STARTED";
    
    

    public static void sendNexusStatusCommand( String command )
    {
        String serviceURI = TestProperties.getString( "nexus.base.url" ) + "service/local/status/command";

        Request request = new Request();

        request.setResourceRef( serviceURI );
        request.setMethod( Method.PUT );
        request.setEntity( command, MediaType.TEXT_ALL );

        Client client = new Client( Protocol.HTTP );

        Response response = client.handle( request );

        if ( !response.getStatus().isSuccess() )
        {
            Assert.fail( "Could not " + command + " Nexus: (" + response.getStatus() + ")" );
        }
    }

    public static void doSoftStop()
    {
        sendNexusStatusCommand( "STOP" );
    }

    public static void doSoftStart()
    {
        sendNexusStatusCommand( "START" );
    }

    public static void doSoftRestart()
    {
        sendNexusStatusCommand( "RESTART" );
    }

    public static boolean isNexusRunning()
        throws IOException
    {
        return ( STATUS_STARTED.equals( getNexusStatus().getData().getState() ) );
    }

    public static StatusResourceResponse getNexusStatus()
        throws IOException
    {
        XStream xstream = new XStream();
        StatusResourceResponse status = null;

        status =
            (StatusResourceResponse) xstream.fromXML( new URL( TestProperties.getString( "nexus.base.url" ) + "service/local/status" ).openStream() );
        return status;
    }
    
}

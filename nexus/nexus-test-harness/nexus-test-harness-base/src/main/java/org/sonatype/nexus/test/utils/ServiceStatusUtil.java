package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;

public class ServiceStatusUtil
{

    protected static Logger log = Logger.getLogger( ServiceStatusUtil.class );

    public static boolean waitForStart( NexusClient client )
        throws NexusClientException, NexusConnectionException
    {
        log.info( "wait for Nexus start" );
        System.setProperty( NexusRestClient.WAIT_FOR_START_TIMEOUT_KEY, "1000" );
        for ( int i = 0; i < 80; i++ )
        {
            log.debug( "wait for Nexus start, attempt: " + i );
            if ( client.isNexusStarted( true ) )
            {
                return true;
            }
        }
        return false;
    }

    public static boolean waitForStop( NexusClient client )
        throws NexusClientException, NexusConnectionException
    {
        log.info( "wait for Nexus stop" );
        System.setProperty( NexusRestClient.WAIT_FOR_START_TIMEOUT_KEY, "1000" );
        for ( int i = 0; i < 80; i++ )
        {
            log.debug( "wait for Nexus stop, attempt: " + i );
            if ( !client.isNexusStarted( true ) )
            {
                return true;
            }
        }
        return false;
    }
}

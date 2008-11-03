package org.sonatype.nexus.test.utils;

import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;

public class ServiceStatusUtil
{

    public static boolean waitForStart( NexusClient client )
        throws NexusClientException, NexusConnectionException
    {
        System.setProperty( NexusRestClient.WAIT_FOR_START_TIMEOUT_KEY, "1000" );
        for ( int i = 0; i < 20; i++ )
        {
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
        System.setProperty( NexusRestClient.WAIT_FOR_START_TIMEOUT_KEY, "500" );
        for ( int i = 0; i < 20; i++ )
        {
            if ( !client.isNexusStarted( true ) )
            {
                return true;
            }
        }
        return false;
    }
}

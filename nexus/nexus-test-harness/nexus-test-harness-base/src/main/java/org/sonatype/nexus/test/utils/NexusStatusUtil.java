/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;
import org.sonatype.nexus.client.NexusClient;
import org.sonatype.nexus.client.NexusClientException;
import org.sonatype.nexus.client.NexusConnectionException;
import org.sonatype.nexus.client.rest.NexusRestClient;

public class NexusStatusUtil
{

    protected static Logger log = Logger.getLogger( NexusStatusUtil.class );

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

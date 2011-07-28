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
package org.sonatype.nexus.plugin.discovery;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.restlight.common.AbstractRESTLightClient;
import org.sonatype.nexus.restlight.common.RESTLightClientException;

@Component( role = NexusTestClientManager.class )
public class DefaultTestClientManager
    implements NexusTestClientManager, LogEnabled
{

    private Logger logger;

    public boolean testConnection( final String url, final String user, final String password )
    {
        try
        {
            new NexusTestClient( url, user, password );
            return true;
        }
        catch ( RESTLightClientException e )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Failed to connect: " + e.getMessage(), e );
            }

            // System.out.println( "Invalid Nexus URL and/or authentication for: " + url + " (user: " + user + ")" );
            logger.info( "Invalid Nexus URL and/or authentication for: " + url + " (user: " + user + ")" );
        }

        return false;
    }

    private static final class NexusTestClient
        extends AbstractRESTLightClient
    {
        protected NexusTestClient( final String baseUrl, final String user, final String password )
            throws RESTLightClientException
        {
            super( baseUrl, user, password, "connectionTest" );
        }
    }

    public void enableLogging( final Logger logger )
    {
        this.logger = logger;
    }

}

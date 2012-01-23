/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

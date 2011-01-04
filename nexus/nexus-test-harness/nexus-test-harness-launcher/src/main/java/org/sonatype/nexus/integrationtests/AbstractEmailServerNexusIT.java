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
package org.sonatype.nexus.integrationtests;

import org.apache.log4j.Logger;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public abstract class AbstractEmailServerNexusIT
    extends AbstractNexusIntegrationTest
{

    private static final Logger LOG = Logger.getLogger( AbstractEmailServerNexusIT.class );

    private static int emailServerPort;

    protected static GreenMail server;

    @BeforeClass
    @org.junit.BeforeClass
    public static void startEmailServer()
    {
        String port = TestProperties.getString( "email.server.port" );
        emailServerPort = new Integer( port );
        // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
        ServerSetup smtp = new ServerSetup( emailServerPort, null, ServerSetup.PROTOCOL_SMTP );

        server = new GreenMail( smtp );
        server.setUser( "system@nexus.org", "smtp-username", "smtp-password" );
        LOG.debug( "Starting e-mail server" );
        server.start();
    }

    @AfterClass
    @org.junit.AfterClass
    public static void stopEmailServer()
    {
        LOG.debug( "Stoping e-mail server" );
        server.stop();
    }
    
    protected boolean waitForMail( int count )
    {
        return waitForMail( count, 5000 );
    }
    
    protected boolean waitForMail( int count, long timeout )
    {
        try
        {
            return server.waitForIncomingEmail( timeout, count );
        }
        catch ( InterruptedException e )
        {
            return false;
        }
    }

}

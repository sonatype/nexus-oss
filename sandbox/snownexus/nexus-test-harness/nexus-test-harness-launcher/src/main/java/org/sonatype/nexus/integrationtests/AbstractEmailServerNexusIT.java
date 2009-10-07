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
package org.sonatype.nexus.integrationtests;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.sonatype.nexus.test.utils.TestProperties;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public abstract class AbstractEmailServerNexusIT
    extends AbstractNexusIntegrationTest
{

    private static final Logger LOG = Logger.getLogger( AbstractEmailServerNexusIT.class );

    private static final int emailServerPort;

    static
    {
        String port = TestProperties.getString( "email.server.port" );
        emailServerPort = new Integer( port );
    }

    protected static GreenMail server;

    @BeforeClass
    public static void startEmailServer()
    {
        // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
        ServerSetup smtp = new ServerSetup( emailServerPort, null, ServerSetup.PROTOCOL_SMTP );

        server = new GreenMail( smtp );
        server.setUser( "system@nexus.org", "smtp-username", "smtp-password" );
        LOG.debug( "Starting e-mail server" );
        server.start();
    }

    @AfterClass
    public static void stopEmailServer()
    {
        LOG.debug( "Stoping e-mail server" );
        server.stop();
    }

}

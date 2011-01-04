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
package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class EmailUtil
{
    public static final int EMAIL_SERVER_PORT;

    private static final Logger log = Logger.getLogger( EmailUtil.class );

    public static final String USER_USERNAME = "smtp-username";

    public static final String USER_PASSWORD = "smtp-password";

    public static final String USER_EMAIL = "system@nexus.org";

    static
    {
        String port = TestProperties.getString( "email.server.port" );
        EMAIL_SERVER_PORT = new Integer( port );
    }

    private static GreenMail server;

    public static synchronized GreenMail startEmailServer()
    {
        if ( server == null )
        {
            // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
            ServerSetup smtp = new ServerSetup( EMAIL_SERVER_PORT, null, ServerSetup.PROTOCOL_SMTP );

            server = new GreenMail( smtp );
            server.setUser( USER_EMAIL, USER_USERNAME, USER_PASSWORD );
            log.debug( "Starting e-mail server" );
            server.start();
        }
        return server;
    }

    public static synchronized void stopEmailServer()
    {
        log.debug( "Stoping e-mail server" );
        server.stop();
        server = null;
    }

}

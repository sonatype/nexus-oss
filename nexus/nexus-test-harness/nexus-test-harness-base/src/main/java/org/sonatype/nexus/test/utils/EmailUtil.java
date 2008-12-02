package org.sonatype.nexus.test.utils;

import org.apache.log4j.Logger;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

public class EmailUtil
{
    private static final int emailServerPort;

    private static final Logger log = Logger.getLogger( EmailUtil.class );

    static
    {
        String port = TestProperties.getString( "email.server.port" );
        emailServerPort = new Integer( port );
    }

    private static GreenMail server;

    public static synchronized GreenMail startEmailServer()
    {
        if ( server == null )
        {
            // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
            ServerSetup smtp = new ServerSetup( emailServerPort, null, ServerSetup.PROTOCOL_SMTP );

            server = new GreenMail( smtp );
            server.setUser( "system@nexus.org", "smtp-username", "smtp-password" );
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

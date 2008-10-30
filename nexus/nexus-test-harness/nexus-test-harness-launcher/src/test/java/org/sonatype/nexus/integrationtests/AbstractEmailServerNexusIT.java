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

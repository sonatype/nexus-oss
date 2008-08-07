package org.sonatype.nexus.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;

public abstract class AbstractEmailServerNexusIT
    extends AbstractNexusIntegrationTest
{

    protected static GreenMail server;

    @BeforeClass
    public static void startEmailServer()
    {
        // ServerSetup smtp = new ServerSetup( 1234, null, ServerSetup.PROTOCOL_SMTP );
        server = new GreenMail( ServerSetupTest.SMTP );
        server.setUser( "system@nexus.org", "smtp-username", "smtp-password" );
        System.out.println( "Starting e-mail server" );
        server.start();
    }

    @AfterClass
    public static void stopEmailServer()
    {
        System.out.println( "Stoping e-mail server" );
        server.stop();
    }

}

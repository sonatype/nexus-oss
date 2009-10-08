package org.sonatype.nexus.integrationtests.nexus1806;

import static org.sonatype.nexus.test.utils.EmailUtil.USER_EMAIL;
import static org.sonatype.nexus.test.utils.EmailUtil.USER_PASSWORD;
import static org.sonatype.nexus.test.utils.EmailUtil.USER_USERNAME;

import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.test.utils.EmailUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class Nexus1806ValidateSmtpConfigurationIT
    extends AbstractNexusIntegrationTest
{

    private static GreenMail changedServer;

    private static int port;

    private static GreenMail originalServer;

    @BeforeClass
    public static void init()
    {
        port = TestProperties.getInteger( "webproxy-server-port" );
        // it is necessary to change port to make sure it worked
        ServerSetup smtp = new ServerSetup( port, null, ServerSetup.PROTOCOL_SMTP );

        changedServer = new GreenMail( smtp );
        changedServer.setUser( USER_EMAIL, USER_USERNAME, USER_PASSWORD );
        log.debug( "Starting e-mail server" );
        changedServer.start();

        originalServer = EmailUtil.startEmailServer();
    }

    @Test
    public void validateChangedSmtp()
        throws Exception
    {
        run( port, changedServer );
    }

    @Test
    public void validateOriginalSmtp()
        throws Exception
    {
        run( EmailUtil.EMAIL_SERVER_PORT, originalServer );
    }

    @Test
    public void invalidServer()
        throws Exception
    {
        SmtpSettingsResource smtpSettings = new SmtpSettingsResource();
        smtpSettings.setHost( "someremote.localhost.com.zh" );
        smtpSettings.setPort( 1234 );
        smtpSettings.setUsername( EmailUtil.USER_USERNAME );
        smtpSettings.setPassword( EmailUtil.USER_PASSWORD );
        smtpSettings.setSystemEmailAddress( EmailUtil.USER_EMAIL );
        smtpSettings.setTestEmail( "test_user@sonatype.org" );
        Status status = SettingsMessageUtil.validateSmtp( smtpSettings );
        Assert.assertEquals( "Unable to validate e-mail " + status, 400, status.getCode() );
    }

    @Test
    public void invalidUsername()
        throws Exception
    {
        if ( true )
        {
            // greenmail doesn't allow authentication
            printKnownErrorButDoNotFail( getClass(), "invalidUsername()" );
            return;
        }

        String login = "invaliduser_test";
        String email = "invaliduser_test@sonatype.org";
        changedServer.setUser( email, login, "%^$@invalidUserPW**" );

        SmtpSettingsResource smtpSettings = new SmtpSettingsResource();
        smtpSettings.setHost( "localhost" );
        smtpSettings.setPort( port );
        smtpSettings.setUsername( login );
        smtpSettings.setPassword( USER_PASSWORD );
        smtpSettings.setSystemEmailAddress( email );
        smtpSettings.setTestEmail( "test_user@sonatype.org" );
        Status status = SettingsMessageUtil.validateSmtp( smtpSettings );
        Assert.assertEquals( "Unable to validate e-mail " + status, 400, status.getCode() );
    }

    private void run( int port, GreenMail server )
        throws IOException, InterruptedException
    {
        SmtpSettingsResource smtpSettings = new SmtpSettingsResource();
        smtpSettings.setHost( "localhost" );
        smtpSettings.setPort( port );
        smtpSettings.setUsername( EmailUtil.USER_USERNAME );
        smtpSettings.setPassword( EmailUtil.USER_PASSWORD );
        smtpSettings.setSystemEmailAddress( EmailUtil.USER_EMAIL );
        smtpSettings.setTestEmail( "test_user@sonatype.org" );
        Status status = SettingsMessageUtil.validateSmtp( smtpSettings );
        Assert.assertTrue( "Unable to validate e-mail " + status, status.isSuccess() );

        server.waitForIncomingEmail( 2000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();
        Assert.assertEquals( 1, msgs.length );

        MimeMessage msg = msgs[0];
        String body = GreenMailUtil.getBody( msg );

        Assert.assertNotNull( "Missing message", body );
        Assert.assertFalse( "Got empty message", body.trim().length() == 0 );
    }

    @AfterClass
    public static void stop()
    {
        EmailUtil.stopEmailServer();
        changedServer.stop();
    }
}

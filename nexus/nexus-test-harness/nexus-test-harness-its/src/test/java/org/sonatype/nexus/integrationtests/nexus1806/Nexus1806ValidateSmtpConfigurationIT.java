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
package org.sonatype.nexus.integrationtests.nexus1806;

import static org.sonatype.nexus.test.utils.EmailUtil.USER_EMAIL;
import static org.sonatype.nexus.test.utils.EmailUtil.USER_PASSWORD;
import static org.sonatype.nexus.test.utils.EmailUtil.USER_USERNAME;

import java.io.IOException;

import javax.mail.internet.MimeMessage;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.SmtpSettingsResource;
import org.sonatype.nexus.test.utils.EmailUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        smtpSettings.setHost( "not:a:server:90854322" );
        smtpSettings.setPort( 1234 );
        smtpSettings.setUsername( EmailUtil.USER_USERNAME );
        smtpSettings.setPassword( EmailUtil.USER_PASSWORD );
        smtpSettings.setSystemEmailAddress( EmailUtil.USER_EMAIL );
        smtpSettings.setTestEmail( "test_user@sonatype.org" );
        Response response = SettingsMessageUtil.validateSmtpResponse( smtpSettings );

        String text = response.getEntity().getText();
        Status status = response.getStatus();
        Assert.assertEquals( status.getCode(), 400, "Unable to validate e-mail " + status + "\n" + text );
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
        Assert.assertEquals( status.getCode(), 400, "Unable to validate e-mail " + status );
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
        Response res = SettingsMessageUtil.validateSmtpResponse( smtpSettings );
        Assert.assertTrue( res.getStatus().isSuccess(), "Unable to validate e-mail " + res.getStatus() + "\n"
            + res.getEntity().getText() );

        server.waitForIncomingEmail( 5000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();
        Assert.assertEquals( 1, msgs.length );

        MimeMessage msg = msgs[0];
        String body = GreenMailUtil.getBody( msg );

        Assert.assertNotNull( "Missing message", body );
        Assert.assertFalse( body.trim().length() == 0, "Got empty message" );
    }

    @AfterClass
    public static void stop()
    {
        EmailUtil.stopEmailServer();
        changedServer.stop();
    }
}

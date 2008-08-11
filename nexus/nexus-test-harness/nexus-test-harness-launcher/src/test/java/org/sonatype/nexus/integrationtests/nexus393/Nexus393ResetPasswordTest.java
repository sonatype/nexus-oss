package org.sonatype.nexus.integrationtests.nexus393;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;

public class Nexus393ResetPasswordTest
    extends AbstractEmailServerNexusIT
{

    @Test
    public void resetPassword()
        throws Exception
    {
        String username = "test-user";
        Status status = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( status.isSuccess() );

        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();
        MimeMessage msg = msgs[0];

        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msg );

        String password = body.substring( body.lastIndexOf( ' ' ) + 1 );
        System.out.println( "New password:\n" + password );

        Assert.assertNotNull( password );
    }

}

package org.sonatype.nexus.integrationtests.nexus395;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;


/**
 * Test forgot username system.  Check if nexus is sending the e-mail. 
 */
public class Nexus395ForgotUsernameTest
    extends AbstractEmailServerNexusIT
{

    @Test
    public void recoverUsername()
        throws Exception
    {
        Status status = ForgotUsernameUtils.recoverUsername( "nexus-dev2@sonatype.org" );
        Assert.assertEquals( Status.SUCCESS_ACCEPTED.getCode(), status.getCode() );

        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();

        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msgs[0] );

        String username = body.substring( body.lastIndexOf( ' ' ) + 1 );
        log.debug( "Username:\n" + username );

        Assert.assertNotNull( username );
    }

}

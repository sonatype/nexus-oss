package org.sonatype.nexus.integrationtests.nexus394;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * Test forgot password system.  Check if nexus is sending the e-mail. 
 */
public class Nexus394ForgotPasswordTest
    extends AbstractEmailServerNexusIT
{
    
    @Test
    public void recoverUserPassword()
        throws Exception
    {
        Response response = ForgotPasswordUtils.recoverUserPassword( "test-user", "nexus-dev2@sonatype.org" );
        Assert.assertEquals( "Status: "+response.getStatus() +"\n"+ response.getEntity().getText(), 202, response.getStatus().getCode() );
        
        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();

        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msgs[0] );

        String password = body.substring( body.lastIndexOf( ' ' ) + 1 );
        log.debug( "New password:\n" + password );

        Assert.assertNotNull( password );
    }

}

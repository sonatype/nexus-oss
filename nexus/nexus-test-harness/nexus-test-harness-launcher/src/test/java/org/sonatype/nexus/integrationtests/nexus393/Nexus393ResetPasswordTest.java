/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.nexus393;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;


/**
 * Test password reset.  Check if nexus is sending the e-mail. 
 */
public class Nexus393ResetPasswordTest
    extends AbstractEmailServerNexusIT
{

    @Test
    public void resetPassword()
        throws Exception
    {
        String username = "test-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( "Status: "+ response.getStatus(), response.getStatus().isSuccess() );

        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();
        MimeMessage msg = msgs[0];

        String password = null;
        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msg );
        
        int index = body.indexOf( "Your new password is: " );
        int passwordStartIndex = index + "Your new password is: ".length();
        if ( index != -1 )
        {
            password = body.substring( passwordStartIndex, body.indexOf( '\n', passwordStartIndex ) ).trim();
            log.debug( "New password:\n" + password );
        }

        Assert.assertNotNull( password );
    }

}

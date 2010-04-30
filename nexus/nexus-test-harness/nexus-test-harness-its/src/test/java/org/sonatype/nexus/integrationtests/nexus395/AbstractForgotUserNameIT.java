/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus395;

import javax.mail.internet.MimeMessage;

import org.junit.Assert;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;

import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * @author juven
 */
public abstract class AbstractForgotUserNameIT
    extends AbstractEmailServerNexusIT
{
    protected void assertRecoveredUserName( String expectedUserName )
        throws Exception
    {
        // Need 1 message
        server.waitForIncomingEmail( 1000, 1 );

        MimeMessage[] msgs = server.getReceivedMessages();

        for ( MimeMessage msg : msgs )
        {
            log.debug( "Mail Title:\n" + GreenMailUtil.getHeaders( msg ) );
            log.debug( "Mail Body:\n" + GreenMailUtil.getBody( msg ) );
        }

        String username = null;
        // Sample body: Your password has been reset. Your new password is: c1r6g4p8l7
        String body = GreenMailUtil.getBody( msgs[0] );

        int index = body.indexOf( " - \"" );
        int usernameStartIndex = index + " - \"".length();
        if ( index != -1 )
        {
            username = body.substring( usernameStartIndex, body.indexOf( '\"', usernameStartIndex ) ).trim();
        }

        Assert.assertEquals( expectedUserName, username );
    }
}

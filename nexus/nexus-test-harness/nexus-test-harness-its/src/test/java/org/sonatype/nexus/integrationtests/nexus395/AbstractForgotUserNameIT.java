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
package org.sonatype.nexus.integrationtests.nexus395;

import javax.mail.internet.MimeMessage;

import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;
import org.testng.Assert;

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
        waitForMail( 1 );

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

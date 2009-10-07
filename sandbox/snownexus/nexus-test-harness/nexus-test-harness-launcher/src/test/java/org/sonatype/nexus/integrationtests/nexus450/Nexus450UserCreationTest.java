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
package org.sonatype.nexus.integrationtests.nexus450;

import javax.mail.internet.MimeMessage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractEmailServerNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.security.rest.model.UserResource;

import com.icegreen.greenmail.util.GreenMailUtil;

/**
 * Using admin account create a new user. Then check for new user creation confirmation e-mail and password. Login and
 * change password. Confirm if it can login.
 */
public class Nexus450UserCreationTest
    extends AbstractEmailServerNexusIT
{

    private UserMessageUtil userUtil;

    private static final String USER_ID = "velo";

    @BeforeClass
    public static void enableSecureContext()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Before
    public void init()
    {
        userUtil =
            new UserMessageUtil( this.getJsonXStream(), MediaType.APPLICATION_JSON );
    }

    @Test
    public void createUser()
        throws Exception
    {
        TestContext testContext = TestContainer.getInstance().getTestContext();
        testContext.useAdminForRequests();

        // create user,
        UserResource resource = new UserResource();
        resource.setUserId( USER_ID );
        resource.setName( "Marvin Velo" );
        resource.setEmail( "velo@earth.com" );
        resource.setStatus( "active" );
        resource.addRole( "admin" );
        userUtil.createUser( resource );

        // get email
        // two e-mails (first confirming user creating and second with users pw)
        server.waitForIncomingEmail( 1000, 2 );
        Thread.sleep( 1000 ); //w8 a few more

        MimeMessage[] msgs = server.getReceivedMessages();
        String password = null;
        StringBuilder emailsContent = new StringBuilder();
        
        /// make sure we have at least 1 message
        Assert.assertTrue( "No emails recieved.", msgs.length > 0 );
        
        for ( MimeMessage mimeMessage : msgs )
        {
            emailsContent.append( GreenMailUtil.getHeaders( mimeMessage ) ).append( '\n' );

            // Sample body: Your new password is ********
            String body = GreenMailUtil.getBody( mimeMessage );
            emailsContent.append( body ).append( '\n' ).append( '\n' );

            int index = body.indexOf( "Your new password is " );
            int passwordStartIndex = index + "Your new password is ".length();
            if ( index != -1 )
            {
                password = body.substring( passwordStartIndex, body.indexOf( '\n', passwordStartIndex ) ).trim();
                log.debug( "New password:\n" + password );
                break;
            }
        }

        Assert.assertNotNull("Didn't recieve a password.  Got the following messages:\n" + emailsContent, password );

        // login with generated password
        testContext.setUsername( USER_ID );
        testContext.setPassword( password );
        Status status = UserCreationUtil.login();
        Assert.assertTrue( status.isSuccess() );

        // set new password
        String newPassword = "velo123";
        status = ChangePasswordUtils.changePassword( USER_ID, password, newPassword );
        Assert.assertTrue( status.isSuccess() );

        // check if the user is 'active'
        testContext.useAdminForRequests();
        UserResource user = userUtil.getUser( USER_ID );
        Assert.assertEquals( "active", user.getStatus() );

        // login with new password
        testContext.setUsername( USER_ID );
        testContext.setPassword( newPassword );
        status = UserCreationUtil.login();
        Assert.assertTrue( status.isSuccess() );
    }

    @After
    public void removeUser()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        UserMessageUtil.removeUser( USER_ID );
    }

}

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
import org.sonatype.nexus.integrationtests.nexus408.ChangePasswordUtils;
import org.sonatype.nexus.rest.model.UserResource;
import org.sonatype.nexus.rest.xstream.XStreamInitializer;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.plexus.rest.xstream.json.JsonOrgHierarchicalStreamDriver;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.thoughtworks.xstream.XStream;

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
            new UserMessageUtil( XStreamInitializer.initialize( new XStream( new JsonOrgHierarchicalStreamDriver() ) ),
                                 MediaType.APPLICATION_JSON );
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

        MimeMessage[] msgs = server.getReceivedMessages();
        String password = null;
        for ( MimeMessage mimeMessage : msgs )
        {
            // Sample body: Your new password is ********
            String body = GreenMailUtil.getBody( mimeMessage );
            if ( body.startsWith( "Your new password is " ) )
            {
                password = body.substring( body.lastIndexOf( ' ' ) + 1 );
                System.out.println( "New password:\n" + password );
                break;
            }
        }

        Assert.assertNotNull( password );

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
        userUtil.removeUser( USER_ID );
    }

}

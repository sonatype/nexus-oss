package org.sonatype.nexus.integrationtests.nexus2862;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.test.utils.UserCreationUtil;

public class Nexus2862UrlRealmTest
    extends AbstractNexusIntegrationTest
{

    private static Integer proxyPort;

    private static AuthenticationServer server;

    static
    {
        proxyPort = TestProperties.getInteger( "proxy.server.port" );
        System.setProperty( "plexus.authentication-url", "http://localhost:" + proxyPort );
        System.setProperty( "plexus.url-authentication-default-role", "admin" );
        System.setProperty( "plexus.url-authentication-email-domain", "sonatype.com" );
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @BeforeClass
    public static void startServer()
        throws Exception
    {
        server = new AuthenticationServer( proxyPort );
        server.addUser( "juka", "juk@", "admin" );
        server.start();
    }

    @AfterClass
    public static void stopServer()
        throws Exception
    {
        server.stop();
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        TestContainer.getInstance().getTestContext().useAdminForRequests();
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();
        resource.getSecurityRealms().clear();
        resource.getSecurityRealms().add( "url" );
        Status status = SettingsMessageUtil.save( resource );
        Assert.assertTrue( status.isSuccess() );
    }

    @After
    public void cleanAccessedUris()
    {
        server.getAccessedUri().clear();
    }

    @Test
    public void loginUrlRealm()
        throws IOException
    {
        Assert.assertTrue( UserCreationUtil.login( "juka", "juk@" ).isSuccess() );

        Assert.assertFalse( server.getAccessedUri().isEmpty() );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

    @Test
    public void wrongPassword()
        throws IOException
    {
        Status status = UserCreationUtil.login( "juka", "juka" );
        Assert.assertFalse( status + "", status.isSuccess() );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

    @Test
    public void wrongUsername()
        throws IOException
    {
        Status status = UserCreationUtil.login( "anuser", "juka" );
        Assert.assertFalse( status + "", status.isSuccess() );

        Assert.assertTrue( UserCreationUtil.logout().isSuccess() );
    }

}

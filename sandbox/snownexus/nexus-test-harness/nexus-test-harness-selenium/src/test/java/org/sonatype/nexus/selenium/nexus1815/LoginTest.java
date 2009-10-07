package org.sonatype.nexus.selenium.nexus1815;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.LoginWindow;
import org.sonatype.nexus.mock.pages.MainPage;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.security.rest.model.AuthenticationClientPermissions;
import org.sonatype.security.rest.model.AuthenticationLoginResource;
import org.sonatype.security.rest.model.AuthenticationLoginResourceResponse;
import org.sonatype.security.rest.model.ClientPermission;
import org.testng.annotations.Test;

@Component( role = LoginTest.class )
public class LoginTest
    extends SeleniumTest
{

    @Test
    public void doLoginTest()
    {
        doLogin( User.ADMIN.getUsername(), User.ADMIN.getPassword() );

        assertFalse( "Login link should not be available", main.loginLinkAvailable() );
    }

    @Test
    public void goodLogin()
    {
        main.clickLogin().populate( User.ADMIN ).loginExpectingSuccess();

        assertFalse( "Login link should not be available", main.loginLinkAvailable() );
    }

    public static void doLogin( MainPage main )
    {
        main.getSelenium().runScript(
                                      "window.Sonatype.utils.doLogin( null, '" + User.ADMIN.getUsername() + "', '"
                                          + User.ADMIN.getPassword() + "');" );
    }

    @Test
    public void missingPassword()
    {
        LoginWindow loginWindow = main.clickLogin().populate( new User( "bad", "" ) );

        assertTrue( "Login button should be disabled if password is bad", loginWindow.getLoginButton().disabled() );
        assertTrue( "Password field should have error message",
                    loginWindow.getPassword().hasErrorText( "This field is required" ) );
    }

    @Test
    public void doListenLoginTest()
    {
        MockHelper.listen( "/authentication/login", new MockListener() );

        doLogin( User.ADMIN.getUsername(), User.ADMIN.getPassword() );

        assertFalse( "Login link should not be available", main.loginLinkAvailable() );

        MockHelper.checkAndClean();
    }

    @Test
    public void doMockLoginTest()
    {
        AuthenticationLoginResourceResponse result = new AuthenticationLoginResourceResponse();
        AuthenticationLoginResource data = new AuthenticationLoginResource();
        AuthenticationClientPermissions permissions = new AuthenticationClientPermissions();
        ClientPermission permission = new ClientPermission();
        permission.setId( "nexus:*" );
        permission.setValue( 0 );
        permissions.addPermission( permission );
        data.setClientPermissions( permissions );
        result.setData( data );

        MockHelper.expect( "/authentication/login", new MockResponse( Status.SUCCESS_OK, result ) );

        doLogin( User.ADMIN.getUsername(), User.ADMIN.getPassword() );

        assertFalse( "Login link should not be available", main.loginLinkAvailable() );

        MockHelper.checkAndClean();
    }

}

package org.sonatype.nexus.selenium.nexus1815;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.LoginWindow;
import org.sonatype.nexus.mock.pages.MainPage;

public class LoginTest
    extends SeleniumTest
{
    @Test
    public void goodLogin()
    {
        doLogin( main );
    }

    public static void doLogin( MainPage main )
    {
        main.clickLogin().populate( User.ADMIN ).loginExpectingSuccess();

        assertFalse( "Login link should not be available", main.loginLinkAvailable() );
    }

    @Test
    public void missingPassword()
    {
        LoginWindow loginWindow = main.clickLogin().populate( new User( "bad", "" ) );

        assertTrue( "Login button should be disabled if password is bad", loginWindow.getLoginButton().disabled() );
        assertTrue( "Password field should have error message",
                    loginWindow.getPassword().hasErrorText( "This field is required" ) );
    }
}

package org.sonatype.nexus.mock;

import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.LoginWindow;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoginTest extends SeleniumTest {
    @Test
    public void goodLogin() {
        main.clickLogin().populate(User.ADMIN).loginExpectingSuccess();

        assertFalse("Login link should not be available", main.loginLinkAvailable());
    }

    @Test
    public void missingPassword() {
        LoginWindow loginWindow = main.clickLogin().populate(new User("bad", ""));

        assertTrue("Login button should be disabled if password is bad", loginWindow.getLoginButton().disabled());
        assertTrue("Password field should have error message", loginWindow.getPassword().hasErrorText("This field is required"));
    }
}

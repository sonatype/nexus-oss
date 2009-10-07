package org.sonatype.nexus.selenium.nexus1815;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.jsecurity.authc.UsernamePasswordToken;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.ChangePasswordWindow;
import org.sonatype.nexus.mock.pages.PasswordChangedWindow;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.authentication.AuthenticationException;
import org.sonatype.security.rest.model.UserChangePasswordRequest;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Component( role = ChangePasswordTest.class )
public class ChangePasswordTest
    extends SeleniumTest
{

    private static final String ORIGINAL_PW = "pwChanger";

    @Requirement
    private SecuritySystem securitySystem;

    private User user;

    @BeforeClass
    public void createUser()
        throws Exception
    {
        user = new DefaultUser();
        user.setUserId( "pwChanger" );
        user.setName( "Password changer" );
        user.setEmailAddress( "email@sonatype.org" );
        user.setRoles( Collections.singleton( new RoleIdentifier( "ui-basic", "ui-basic" ) ) );
        user.setStatus( UserStatus.active );
        user.setSource( "default" );
        securitySystem.addUser( user, ORIGINAL_PW );
    }

    @AfterClass
    public void deleteUser()
        throws UserNotFoundException
    {
        securitySystem.deleteUser( user.getUserId() );
    }

    @Test
    public void changePasswordSuccess()
        throws AuthenticationException
    {
        main.clickLogin().populate( user.getUserId(), ORIGINAL_PW ).loginExpectingSuccess();

        ChangePasswordWindow window = main.securityPanel().clickChangePassword();

        final String newPw = "newPassword";
        MockHelper.listen( "/users_changepw", new MockListener()
        {
            @Override
            protected void onPayload( Object payload )
            {
                UserChangePasswordRequest r = (UserChangePasswordRequest) payload;
                assertEquals( ORIGINAL_PW, r.getData().getOldPassword() );
                assertEquals( newPw, r.getData().getNewPassword() );
            }
        } );

        PasswordChangedWindow passwordChangedWindow =
            window.populate( ORIGINAL_PW, newPw, newPw ).changePasswordExpectingSuccess();

        passwordChangedWindow.clickOk();

        MockHelper.checkAndClean();

        main.clickLogout();

        main.clickLogin().populate( user.getUserId(), newPw ).loginExpectingSuccess();
        securitySystem.authenticate( new UsernamePasswordToken( user.getUserId(), newPw ) );
    }
}

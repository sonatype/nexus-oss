package org.sonatype.nexus.selenium.nexus1815;

import static org.testng.AssertJUnit.assertEquals;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.ChangePasswordWindow;
import org.sonatype.nexus.mock.pages.PasswordChangedWindow;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.security.rest.model.UserChangePasswordRequest;
import org.testng.annotations.Test;

@Component( role = ChangePasswordTest.class )
public class ChangePasswordTest
    extends SeleniumTest
{

    @Test
    public void changePasswordSuccess()
    {
        main.clickLogin().populate( User.ROLE_ADMIN ).loginExpectingSuccess();

        ChangePasswordWindow window = main.securityPanel().clickChangePassword();

        MockHelper.expect( "/users_changepw", new MockResponse( Status.SUCCESS_NO_CONTENT, null )
        {
            @Override
            public void setPayload( Object payload )
                throws AssertionError
            {
                UserChangePasswordRequest r = (UserChangePasswordRequest) payload;
                assertEquals( "password", r.getData().getOldPassword() );
                assertEquals( "newPassword", r.getData().getNewPassword() );
            }
        } );

        PasswordChangedWindow passwordChangedWindow =
            window.populate( "password", "newPassword", "newPassword" ).changePasswordExpectingSuccess();

        passwordChangedWindow.clickOk();
    }
}

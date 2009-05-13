package org.sonatype.security.mock;

import java.util.List;

import org.sonatype.security.email.SecurityEmailer;

public class MockEmailer
    implements SecurityEmailer
{

    public List<String> forgotUserIds;

    public void sendForgotUsername( String email, List<String> userIds )
    {
        forgotUserIds = userIds;
    }

    public void sendNewUserCreated( String email, String userid, String password )
    {
    }

    public void sendResetPassword( String email, String password )
    {
    }

    public List<String> getForgotUserIds()
    {
        return forgotUserIds;
    }

    public void setForgotUserIds( List<String> forgotUserIds )
    {
        this.forgotUserIds = forgotUserIds;
    }

}

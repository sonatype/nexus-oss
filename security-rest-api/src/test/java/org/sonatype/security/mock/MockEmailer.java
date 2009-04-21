package org.sonatype.security.mock;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.email.SecurityEmailer;

@Component( role = SecurityEmailer.class )
public class MockEmailer
    implements SecurityEmailer
{

    public void sendForgotUsername( String arg0, List<String> arg1 )
    {
        // TODO Auto-generated method stub

    }

    public void sendNewUserCreated( String arg0, String arg1, String arg2 )
    {
        // TODO Auto-generated method stub

    }

    public void sendResetPassword( String arg0, String arg1 )
    {
        // TODO Auto-generated method stub

    }

}

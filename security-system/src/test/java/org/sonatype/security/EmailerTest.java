package org.sonatype.security;

import junit.framework.Assert;

import org.sonatype.security.email.SecurityEmailer;
import org.sonatype.security.mock.MockEmailer;
import org.sonatype.security.usermanagement.UserNotFoundException;

public class EmailerTest
    extends AbstractSecurityTest
{

    public void testForgotUsername()
        throws Exception
    {
        // use our Mock emailer
        MockEmailer emailer = (MockEmailer) this.lookup( SecurityEmailer.class );

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        securitySystem.forgotUsername( "cdugas@sonatype.org" );

        Assert.assertTrue( emailer.getForgotUserIds().contains( "cdugas" ) );
        Assert.assertEquals( 1, emailer.getForgotUserIds().size() );
    }

    public void testDoNotRecoverAnonUserName()
        throws Exception
    {
        // use our Mock emailer
        MockEmailer emailer = (MockEmailer) this.lookup( SecurityEmailer.class );

        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        try
        {
            securitySystem.forgotUsername( "anonymous@sonatype.org" );
            Assert.fail( "UserNotFoundException expected" );
        }
        catch ( UserNotFoundException e )
        {
            // expected
        }

    }

}

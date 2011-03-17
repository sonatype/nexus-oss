package org.sonatype.security;

import junit.framework.Assert;

import org.sonatype.security.email.SecurityEmailer;
import org.sonatype.security.mock.MockEmailer;
import org.sonatype.security.usermanagement.UserNotFoundException;

import com.google.inject.Binder;

public class EmailerTest
    extends AbstractSecurityTest
{
    private MockEmailer emailer = new MockEmailer();

    @Override
    public void configure( Binder binder )
    {
        binder.bind( SecurityEmailer.class ).toInstance( emailer );
    }

    public void testForgotUsername()
        throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );

        securitySystem.forgotUsername( "cdugas@sonatype.org" );

        Assert.assertTrue( ( (MockEmailer) emailer ).getForgotUserIds().contains( "cdugas" ) );
        Assert.assertEquals( 1, ( (MockEmailer) emailer ).getForgotUserIds().size() );
    }

    public void testDoNotRecoverAnonUserName()
        throws Exception
    {
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

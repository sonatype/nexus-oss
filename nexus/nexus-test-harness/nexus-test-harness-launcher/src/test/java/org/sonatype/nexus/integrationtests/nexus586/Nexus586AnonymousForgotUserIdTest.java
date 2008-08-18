package org.sonatype.nexus.integrationtests.nexus586;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Nexus586AnonymousForgotUserIdTest
    extends AbstractNexusIntegrationTest
{

    static
    {
        printKnownErrorButDoNotFail( Nexus586AnonymousResetPasswordTest.class, "forgotUsername" );
    }

    @Test
    public void forgotUsername()
        throws Exception
    {
        // Status status = ForgotUsernameUtils.recoverUsername( "changeme2@yourcompany.com" );
        // Assert.assertEquals( 400, status.getCode() );
    }
}

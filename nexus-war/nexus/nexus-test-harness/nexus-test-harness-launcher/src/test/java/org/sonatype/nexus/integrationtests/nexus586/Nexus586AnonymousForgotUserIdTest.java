package org.sonatype.nexus.integrationtests.nexus586;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus395.ForgotUsernameUtils;

/**
 * Saving the Nexus config needs to validate the anonymous user information 
 */
public class Nexus586AnonymousForgotUserIdTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void forgotUsername()
        throws Exception
    {
        if( printKnownErrorButDoNotFail( Nexus586AnonymousResetPasswordTest.class, "forgotUsername" ))
        {
            return;
        }
         Status status = ForgotUsernameUtils.recoverUsername( "changeme2@yourcompany.com" );
         Assert.assertEquals( 400, status.getCode() );
    }
}
